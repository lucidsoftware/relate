package com.lucidchart.open.relate

import java.sql.{Connection, PreparedStatement, ResultSet}
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable

/** A query object that can be expanded */
private[relate] case class ExpandableQuery(
  query: String,
  listParams: mutable.Map[String, ListParam] = mutable.Map[String, ListParam]()
) extends ParameterizableSql with Expandable {

  val timeout: Option[Int] = None

  val params = Nil
  protected[relate] def queryParams = QueryParams(
    query,
    params,
    listParams
  )

  protected def setTimeout(stmt: PreparedStatement): Unit = {
    for {
      seconds <- timeout
      stmt <- Option(stmt)
    } yield (stmt.setQueryTimeout(seconds))
  }

  def withTimeout(seconds: Int): ExpandableQuery = new ExpandableQuery(query, listParams) {
    override val timeout: Option[Int] = Some(seconds)

    override def applyParams(stmt: PreparedStatement) {
      setTimeout(stmt)

      super.applyParams(stmt)
    }
  }

  /**
   * The copy method used by the Sql Trait
   * Returns a SqlQuery object so that expansion can only occur before the 'on' method
   */
  protected def getCopy(p: List[SqlStatement => Unit]): SqlQuery = SqlQuery(query, p, listParams)
}


/** A query object that has not had parameter values substituted in */
private[relate] case class SqlQuery(
  query: String,
  params: List[SqlStatement => Unit] = Nil,
  listParams: mutable.Map[String, ListParam] = mutable.Map[String, ListParam]()
) extends ParameterizableSql {

  protected[relate] def queryParams = QueryParams(
    query,
    params,
    listParams
  )

  /**
   * Copy method for the Sql trait
   */
  protected def getCopy(p: List[SqlStatement => Unit]): SqlQuery = copy(params = p)

}

/** The base class for all list type parameters. Contains a name and a count (total number of 
inserted params) */
private[relate] sealed trait ListParam {
  val name: String
  val count: Int
  val charCount: Int
}

/** ListParam type that represents a comma separated list of parameters */
private[relate] case class CommaSeparated(
  name: String,
  count: Int,
  charCount: Int
) extends ListParam

/** ListParam type that represents a comma separated list of tuples */
private[relate] case class Tupled(
  name: String,
  count: Int,
  charCount: Int,
  params: Map[String, Int],
  numTuples: Int,
  tupleSize: Int
) extends ListParam

/** 
 * Expandable is a trait for SQL queries that can be expanded.
 *
 * It defines two expansion methods:
 *  - [[com.lucidchart.open.relate.Expandable#commaSeparated commaSeparated]] for expanding a parameter into a comma separated list
 *  - [[com.lucidchart.open.relate.Expandable#tupled tupled]] for expanding a parameter into a comma separated list of tuples for insertion queries
 *
 * These methods should be called in the [[com.lucidchart.open.relate.Expandable#expand expand]] method.
 * {{{
 * import com.lucidchart.open.relate._
 * import com.lucidchart.open.relate.Query._
 * 
 * val ids = Array(1L, 2L, 3L)  
 *
 * SQL("""
 *   SELECT *
 *   FROM users
 *   WHERE id IN ({ids})
 * """).expand { implicit query =>
 *   commaSeparated("ids", ids.size)
 * }.on {
 *   longs("ids", ids) 
 * }
 * }}}
 */
sealed trait Expandable extends ParameterizableSql {

  /** The names of list params mapped to their size */
  val listParams: mutable.Map[String, ListParam]

  /**
   * Expand out the query by turning an TraversableOnce into several parameters
   * @param a function that will operate by calling functions on this Expandable instance
   * @return a copy of this Expandable with the query expanded
   */
  def expand(f: Expandable => Unit): Expandable = {
    f(this)
    this
  }

  /**
   * Replace the provided identifier with a comma separated list of parameters
   * WARNING: modifies this Expandable in place
   * @param name the identifier for the parameter
   * @param count the count of parameters in the list
   */
  def commaSeparated(name: String, count: Int) {
    listParams(name) = CommaSeparated(name, count, count * 2)
  }

  /**
   * Shorthand for calling [[com.lucidchart.open.relate.Expandable#commaSeparated commaSeparated]]
   * within an [[com.lucidchart.open.relate.Expandable#expand expand]] block.
   * {{{
   * SQL("SELECT * FROM users WHERE id IN ({ids})").commas("ids", 10)
   * }}}
   * is equivalent to
   * {{{
   * SQL("SELECT * FROM users WHERE id IN ({ids})").expand { implicit query =>
   *   commaSeparated("ids", 10)
   * }
   * }}}
   * WARNING: modifies this Expandable in place
   * @param name the parameter name to expand
   * @param count the number of items in the comma separated list
   * @return this Expandable with the added list parameters
   */
  def commas(name: String, count: Int): Expandable = {
    expand(_.commaSeparated(name, count))
    this
  }

  /**
   * Replace the provided identifier with a comma separated list of tuples
   * WARNING: modifies this Expandable in place
   * @param name the identifier for the tuples
   * @param columns a list of the column names in the order they should be inserted into
   * the tuples
   * @param count the number of tuples to insert
   */
  def tupled(name: String, columns: Seq[String], count: Int) {
    val namesToIndexes = columns.zipWithIndex.toMap
    listParams(name) = Tupled(
      name,
      count * columns.size,
      count * 3 + count * columns.size * 2,
      namesToIndexes,
      count,
      columns.size
    )
  }

}

/** A case class that bundles all parameters associated with a query for easy parameter passing */
private[relate] case class QueryParams(
  query: String,
  params: List[SqlStatement => Unit],
  listParams: mutable.Map[String, ListParam]
)

trait ParameterizedSql extends Sql {

  protected[relate] def queryParams: QueryParams

  protected val (parsedQuery, parsedParams) = SqlStatementParser.parse(queryParams.query, queryParams.listParams)

  protected def applyParams(stmt: PreparedStatement) {
    val sqlStmt = new SqlStatement(stmt, parsedParams, queryParams.listParams)
    queryParams.params.reverse.foreach { f =>
      f(sqlStmt)
    }
  }

}

trait ParameterizableSql extends ParameterizedSql {

  /** A list of anonymous functions that insert parameters into a SqlStatement */
  protected val params: List[SqlStatement => Unit]

  /**
   * Classes that inherit the Sql trait will have to implement a method to copy
   * themselves given just a different set of parameters. HINT: Use a case class!
   */
  protected def getCopy(params: List[SqlStatement => Unit]): ParameterizableSql

  /**
   * Put in values for parameters in the query
   * @param f a function that takes a SqlStatement and sets parameter values using its methods
   * @return a copy of this Sql with the new params
   */
  def on(f: SqlStatement => Unit): ParameterizableSql = {
    getCopy(f +: params)
  }

  /**
   * Put in values for tuple parameters in the query
   * @param name the tuple identifier in the query
   * @param tuples the objects to loop over and use to insert data into the query
   * @param f a function that takes a TupleStatement and sets parameter values using its methods
   * @return a copy of this Sql with the new tuple params
   */
  def onTuples[A](name: String, tuples: TraversableOnce[A])(f: (A, TupleStatement) => Unit): ParameterizableSql = {
    val callback: SqlStatement => Unit = { statement =>
      val iterator1 = statement.names(name).toIterator
      val tupleData = statement.listParams(name).asInstanceOf[Tupled]

      while(iterator1.hasNext) {
        var i = iterator1.next
        val iterator2 = tuples.toIterator
        while(iterator2.hasNext) {
          f(iterator2.next, TupleStatement(statement.stmt, tupleData.params, i))
          i += tupleData.tupleSize
        }
      }
    }
    getCopy(callback +: params)
  }
}

/**
 * Sql is a trait for basic SQL queries.
 *
 * It provides methods for parameter insertion and query execution.
 * {{{
 * import com.lucidchart.open.relate._
 * import com.lucidchart.open.relate.Query._
 *
 * case class User(id: Long, name: String)
 *
 * SQL("""
 *   SELECT id, name
 *   FROM users
 *   WHERE id={id}
 * """).on { implicit query =>
 *   long("id", 1L)
 * }.asSingle(RowParser { row =>
 *   User(row.long("id"), row.string("name"))
 * })
 * }}}
 */
trait Sql {
  self =>

  protected val parsedQuery: String
  protected def applyParams(stmt: PreparedStatement)

  protected[relate] class BaseStatement(val connection: Connection) {
    protected val parsedQuery = self.parsedQuery
    protected def applyParams(stmt: PreparedStatement) = self.applyParams(stmt)
  }

  protected def normalStatement(implicit connection: Connection) = new BaseStatement(connection) with NormalStatementPreparer

  protected def insertionStatement(implicit connection: Connection) = new BaseStatement(connection) with InsertionStatementPreparer

  protected def streamedStatement(fetchSize: Int)(implicit connection: Connection) = {
    val fetchSize_ = fetchSize
    new BaseStatement(connection) with StreamedStatementPreparer {
      protected val fetchSize = fetchSize_
    }
  }

  /**
   * Returns the SQL query, before parameter substitution.
   */
  override def toString = parsedQuery

  /**
   * Calls [[PreparedStatement#toString]], which for many JDBC implementations is the SQL query after parameter substitution.
   * This is intended primarily for ad-hoc debugging.
   *
   * For more routine logging, consider other solutions, such as [[https://code.google.com/p/log4jdbc/ log4jdbc]].
   */
  def statementString(implicit connection: Connection) = {
    val stmt = normalStatement.stmt
    val string = stmt.toString
    stmt.close()
    string
  }

  /**
    * Provides direct access to the underlying java.sql.ResultSet.
    * Note that this ResultSet must be closed manually or by wrapping it in SqlResult.
    * {{{
    * val results = SQL(query).results()
    * . . .
    * SqlResult(results).asList[A](parser)
    * // or
    * results.close()
    * }}}
    * @return java.sql.ResultSet
   */
  def results()(implicit connection: Connection): ResultSet = normalStatement.results()

  /**
   * Execute a statement
   * @param connection the db connection to use when executing the query
   * @return whether the query succeeded in its execution
   */
  def execute()(implicit connection: Connection): Boolean = normalStatement.execute()

  /**
   * Execute an update
   * @param connection the db connection to use when executing the query
   * @return the number of rows update by the query
   */
  def executeUpdate()(implicit connection: Connection): Int = normalStatement.executeUpdate()

  /**
   * Execute the query and get the auto-incremented key as an Int
   * @param connection the connection to use when executing the query
   * @return the auto-incremented key as an Int
   */
  def executeInsertInt()(implicit connection: Connection): Int = insertionStatement.execute(_.asSingle(RowParser.insertInt))
  
  /**
   * Execute the query and get the auto-incremented keys as a List of Ints
   * @param connection the connection to use when executing the query
   * @return the auto-incremented keys as a List of Ints
   */
  def executeInsertInts()(implicit connection: Connection): List[Int] = insertionStatement.execute(_.asList(RowParser.insertInt))
  
  /**
   * Execute the query and get the auto-incremented key as a Long
   * @param connection the connection to use when executing the query
   * @return the auto-incremented key as a Long
   */
  def executeInsertLong()(implicit connection: Connection): Long = insertionStatement.execute(_.asSingle(RowParser.insertLong))
  
  /**
   * Execute the query and get the auto-incremented keys as a a List of Longs
   * @param connection the connection to use when executing the query
   * @return the auto-incremented keys as a a List of Longs
   */
  def executeInsertLongs()(implicit connection: Connection): List[Long] = insertionStatement.execute(_.asList(RowParser.insertLong))

  /**
   * Execute the query and get the auto-incremented key using a RowParser. Provided for the case
   * that a primary key is not an Int or BigInt
   * @param parser the RowParser that can parse the returned key
   * @param connection the connection to use when executing the query
   * @return the auto-incremented key
   */
  def executeInsertSingle[U](parser: SqlResult => U)(implicit connection: Connection): U = insertionStatement.execute(_.asSingle(parser))
  
  /**
   * Execute the query and get the auto-incremented keys using a RowParser. Provided for the case
   * that a primary key is not an Int or BigInt
   * @param parser the RowParser that can parse the returned keys
   * @param connection the connection to use when executing the query
   * @return the auto-incremented keys
   */
  def executeInsertCollection[U, T[_]](parser: SqlResult => U)(implicit cbf: CanBuildFrom[T[U], U, T[U]], connection: Connection): T[U] = insertionStatement.execute(_.asCollection(parser))

  /**
   * Execute this query and get back the result as a single record
   * @param parser the RowParser to use when parsing the result set
   * @param connection the connection to use when executing the query
   * @return the results as a single record
   */
  def asSingle[A](parser: SqlResult => A)(implicit connection: Connection): A = normalStatement.execute(_.asSingle(parser))
  def asSingle[A: Parseable]()(implicit connection: Connection): A = normalStatement.execute(_.asSingle[A])

  /**
   * Execute this query and get back the result as an optional single record
   * @param parser the RowParser to use when parsing the result set
   * @param connection the connection to use when executing the query
   * @return the results as an optional single record
   */
  def asSingleOption[A](parser: SqlResult => A)(implicit connection: Connection): Option[A] = normalStatement.execute(_.asSingleOption(parser))
  def asSingleOption[A: Parseable]()(implicit connection: Connection): Option[A] = normalStatement.execute(_.asSingleOption[A])

  /**
   * Execute this query and get back the result as a Set of records
   * @param parser the RowParser to use when parsing the result set
   * @param connection the connection to use when executing the query
   * @return the results as a Set of records
   */
  def asSet[A](parser: SqlResult => A)(implicit connection: Connection): Set[A] = normalStatement.execute(_.asSet(parser))
  def asSet[A: Parseable]()(implicit connection: Connection): Set[A] = normalStatement.execute(_.asSet[A])

  /**
   * Execute this query and get back the result as a sequence of records
   * @param parser the RowParser to use when parsing the result set
   * @param connection the connection to use when executing the query
   * @return the results as a sequence of records
   */
  def asSeq[A](parser: SqlResult => A)(implicit connection: Connection): Seq[A] = normalStatement.execute(_.asSeq(parser))
  def asSeq[A: Parseable]()(implicit connection: Connection): Seq[A] = normalStatement.execute(_.asSeq[A])

  /**
   * Execute this query and get back the result as an iterable of records
   * @param parser the RowParser to use when parsing the result set
   * @param connection the connection to use when executing the query
   * @return the results as an iterable of records
   */
  def asIterable[A](parser: SqlResult => A)(implicit connection: Connection): Iterable[A] = normalStatement.execute(_.asIterable(parser))
  def asIterable[A: Parseable]()(implicit connection: Connection): Iterable[A] = normalStatement.execute(_.asIterable[A])

  /**
   * Execute this query and get back the result as a List of records
   * @param parser the RowParser to use when parsing the result set
   * @param connection the connection to use when executing the query
   * @return the results as a List of records
   */
  def asList[A](parser: SqlResult => A)(implicit connection: Connection): List[A] = normalStatement.execute(_.asList(parser))
  def asList[A: Parseable]()(implicit connection: Connection): List[A] = normalStatement.execute(_.asList[A])

  /**
   * Execute this query and get back the result as a Map of records
   * @param parser the RowParser to use when parsing the result set. The RowParser should return a Tuple
   * of size 2 containing the key and value
   * @param connection the connection to use when executing the query
   * @return the results as a Map of records
   */
  def asMap[U, V](parser: SqlResult => (U, V))(implicit connection: Connection): Map[U, V] = normalStatement.execute(_.asMap(parser))
  def asMap[U, V]()(implicit connection: Connection, p: Parseable[(U, V)]): Map[U, V] = normalStatement.execute(_.asMap[U, V])

  def asMultiMap[U, V](parser: SqlResult => (U, V))(implicit connection: Connection): Map[U, Set[V]] = normalStatement.execute(_.asMultiMap(parser))
  def asMultiMap[U, V]()(implicit connection: Connection, p: Parseable[(U, V)]): Map[U, Set[V]] = normalStatement.execute(_.asMultiMap[U, V])

  /**
   * Execute this query and get back the result as a single value. Assumes that there is only one
   * row and one value in the result set.
   * @param connection the connection to use when executing the query
   * @return the results as a single value
   */
  def asScalar[A]()(implicit connection: Connection): A = normalStatement.execute(_.asScalar[A]())
  
  /**
   * Execute this query and get back the result as an optional single value. Assumes that there is 
   * only one row and one value in the result set.
   * @param parser the RowParser to use when parsing the result set
   * @param connection the connection to use when executing the query
   * @return the results as an optional single value
   */
  def asScalarOption[A]()(implicit connection: Connection): Option[A] = normalStatement.execute(_.asScalarOption[A]())
  
  /**
   * Execute this query and get back the result as an arbitrary collection of records
   * @param parser the RowParser to use when parsing the result set
   * @param connection the connection to use when executing the query
   * @return the results as an arbitrary collection of records
   */
  def asCollection[U, T[_]](parser: SqlResult => U)(implicit cbf: CanBuildFrom[T[U], U, T[U]], connection: Connection): T[U] = normalStatement.execute(_.asCollection(parser))
  def asCollection[U: Parseable, T[_]]()(implicit cbf: CanBuildFrom[T[U], U, T[U]], connection: Connection): T[U] = normalStatement.execute(_.asCollection[U, T])

  /**
   * Execute this query and get back the result as an arbitrary collection of key value pairs
   * @param parser the RowParser to use when parsing the result set
   * @param connection the connection to use when executing the query
   * @return the results as an arbitrary collection of key value pairs
   */
  def asPairCollection[U, V, T[_, _]](parser: SqlResult => (U, V))(implicit cbf: CanBuildFrom[T[U, V], (U, V), T[U, V]], connection: Connection): T[U, V] = normalStatement.execute(_.asPairCollection(parser))
  def asPairCollection[U, V, T[_, _]]()(implicit cbf: CanBuildFrom[T[U, V], (U, V), T[U, V]], connection: Connection, p: Parseable[(U, V)]): T[U, V] = normalStatement.execute(_.asPairCollection[U, V, T])

  /**
   * The asIterator method returns an Iterator that will stream data out of the database.
   * This avoids an OutOfMemoryError when dealing with large datasets. Bear in mind that many
   * JDBC implementations will not allow additional queries to the connection before all records
   * in the Iterator have been retrieved.
   * @param parser the RowParser to parse rows with
   * @param fetchSize the number of rows to fetch at a time, defaults to 100. If the JDBC Driver
   * is MySQL, the fetchSize will always default to Int.MinValue, as MySQL's JDBC implementation
   * ignores all other fetchSize values and only streams if fetchSize is Int.MinValue
   */
  def asIterator[A](parser: SqlResult => A, fetchSize: Int = 100)(implicit connection: Connection): Iterator[A] = {
    val prepared = streamedStatement(fetchSize)
    prepared.execute(RowIterator(parser, prepared.stmt, _))
  }
}
