package com.lucidchart.open.relate

import java.sql.{Connection, PreparedStatement, Statement}
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable

/** A query object that can be expanded */
private[relate] case class ExpandableQuery(
  query: String,
  listParams: mutable.Map[String, ListParam] = mutable.Map[String, ListParam]()
) extends Sql with Expandable {

  val params = Nil

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
) extends Sql {

  /**
   * Copy method for the Sql trait
   */
  protected def getCopy(p: List[SqlStatement => Unit]): SqlQuery = copy(params = p)

}

/** The base class for all list type parameters. Contains a name and a count (total number of 
inserted params) */
sealed trait ListParam {
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

/** A trait for SQL queries that can be expanded */
sealed trait Expandable extends Sql {

  /** The names of list params mapped to their size */
  val listParams: mutable.Map[String, ListParam]

  /**
   * Expand out the query by turning an Iterable into several parameters
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

/** A trait for queries */
sealed trait Sql {

  val query: String
  val params: List[SqlStatement => Unit]
  val listParams: mutable.Map[String, ListParam]

  /**
   * Classes that inherit the Sql trait will have to implement a method to copy
   * themselves given just a different set of parameters. HINT: Use a case class!
   */
  protected def getCopy(params: List[SqlStatement => Unit]): Sql

  /**
   * Put in values for parameters in the query
   * @param f a function that takes a SqlStatement and sets parameter values using its methods
   * @return a copy of this Sql with the new params
   */
  def on(f: SqlStatement => Unit): Sql = {
    getCopy(f +: params)
  }

  /**
   * Put in values for tuple parameters in the query
   * @param name the tuple identifier in the query
   * @param tuples the objects to loop over and use to insert data into the query
   * @param f a function that takes a TupleStatement and sets parameter values using its methods
   * @return a copy of this Sql with the new tuple params
   */
  def onTuples[A](name: String, tuples: TraversableOnce[A])(f: (A, TupleStatement) => Unit): Sql = {
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

  /**
   * Get a completed SqlStatement objecct to execute
   * @param getGeneratedKeys whether or not to get the generated keys
   * @return the prepared SqlStatement
   */
  private def prepareSqlStatement(connection: Connection, getGeneratedKeys: Boolean = false): 
    PreparedStatement = {
    val (parsedQuery, parsedParams) = SqlStatementParser.parse(query, listParams)
    val stmt = if (getGeneratedKeys) {
      connection.prepareStatement(parsedQuery, Statement.RETURN_GENERATED_KEYS)
    }
    else {
      connection.prepareStatement(parsedQuery)
    }
    applyParams(new SqlStatement(stmt, parsedParams, listParams))
    stmt
  }

  /**
   * Apply all the functions that put in parameters
   * @param stmt the SqlStatement to apply the functions to
   * @param the prepared SqlStatement
   */
  private def applyParams(stmt: SqlStatement): SqlStatement = {
    params.reverse.foreach { f =>
      f(stmt)
    }
    stmt
  }

  /**
   * Execute a statement
   */
  def execute()(implicit connection: Connection): Boolean = {
    val stmt = prepareSqlStatement(connection)
    try {
      stmt.execute()
    }
    finally {
      stmt.close()
    }
  }

  /**
   * Execute an update
   */
  def executeUpdate()(implicit connection: Connection): Int = {
    val stmt = prepareSqlStatement(connection)
    try {
      stmt.executeUpdate()
    }
    finally {
      stmt.close()
    }
  }

  /**
   * Execute a query
   */
  @deprecated("Use asList, asMap, or one of the other as* functions instead. This executeQuery function may leak connections", "1.1")
  def executeQuery()(implicit connection: Connection): SqlResult = {
    val stmt = prepareSqlStatement(connection)
    SqlResult(stmt.executeQuery())
  }

  /**
   * Execute an insert
   */
  @deprecated("Use executeInsertLong, executeInsertSingle, or one of the other executeInsert* functions instead. This executeInsert function may leak connections.", "1.1")
  def executeInsert()(implicit connection: Connection): SqlResult = {
    val stmt = prepareSqlStatement(connection, true)
    SqlResult(stmt.getGeneratedKeys())
  }

  def executeInsertInt()(implicit connection: Connection): Int = withExecutedResults(true)(_.asSingle(RowParser.insertInt))
  def executeInsertInts()(implicit connection: Connection): List[Int] = withExecutedResults(true)(_.asList(RowParser.insertInt))
  def executeInsertLong()(implicit connection: Connection): Long = withExecutedResults(true)(_.asSingle(RowParser.insertLong))
  def executeInsertLongs()(implicit connection: Connection): List[Long] = withExecutedResults(true)(_.asList(RowParser.insertLong))

  def executeInsertSingle[U](parser: RowParser[U])(implicit connection: Connection): U = withExecutedResults(true)(_.asSingle(parser))
  def executeInsertCollection[U, T[_]](parser: RowParser[U])(implicit cbf: CanBuildFrom[T[U], U, T[U]], connection: Connection): T[U] = withExecutedResults(true)(_.asCollection(parser))

  protected def withExecutedResults[A](getGeneratedKeys: Boolean)(callback: (SqlResult) => A)(implicit connection: Connection): A = {
    val stmt = prepareSqlStatement(connection, getGeneratedKeys)
    try {
      val resultSet = if (getGeneratedKeys) {
        stmt.executeUpdate()
        stmt.getGeneratedKeys()
      }
      else {
        stmt.executeQuery()
      }

      try {
        callback(SqlResult(resultSet))
      }
      finally {
        resultSet.close()
      }
    }
    finally {
      stmt.close()
    }
  }

  def asSingle[A](parser: RowParser[A])(implicit connection: Connection): A = withExecutedResults(false)(_.asSingle(parser))
  def asSingleOption[A](parser: RowParser[A])(implicit connection: Connection): Option[A] = withExecutedResults(false)(_.asSingleOption(parser))
  def asSet[A](parser: RowParser[A])(implicit connection: Connection): Set[A] = withExecutedResults(false)(_.asSet(parser))
  def asSeq[A](parser: RowParser[A])(implicit connection: Connection): Seq[A] = withExecutedResults(false)(_.asSeq(parser))
  def asIterable[A](parser: RowParser[A])(implicit connection: Connection): Iterable[A] = withExecutedResults(false)(_.asIterable(parser))
  def asList[A](parser: RowParser[A])(implicit connection: Connection): List[A] = withExecutedResults(false)(_.asList(parser))
  def asMap[U, V](parser: RowParser[(U, V)])(implicit connection: Connection): Map[U, V] = withExecutedResults(false)(_.asMap(parser))
  def asScalar[A]()(implicit connection: Connection): A = withExecutedResults(false)(_.asScalar[A]())
  def asScalarOption[A]()(implicit connection: Connection): Option[A] = withExecutedResults(false)(_.asScalarOption[A]())
  def asCollection[U, T[_]](parser: RowParser[U])(implicit cbf: CanBuildFrom[T[U], U, T[U]], connection: Connection): T[U] = withExecutedResults(false)(_.asCollection(parser))
  def asPairCollection[U, V, T[_, _]](parser: RowParser[(U, V)])(implicit cbf: CanBuildFrom[T[U, V], (U, V), T[U, V]], connection: Connection): T[U, V] = withExecutedResults(false)(_.asPairCollection(parser))

}
