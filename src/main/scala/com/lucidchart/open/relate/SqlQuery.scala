package com.lucidchart.open.relate

import java.sql.{Connection, PreparedStatement, ResultSet, Statement}
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
   * Execute a statement
   */
  def execute()(implicit connection: Connection): Boolean = {
    NormalStatementPreparer(connection).execute()
  }

  /**
   * Execute an update
   */
  def executeUpdate()(implicit connection: Connection): Int = {
    NormalStatementPreparer(connection).executeUpdate()
  }

  def executeInsertInt()(implicit connection: Connection): Int = InsertionStatementPreparer(connection).execute(_.asSingle(RowParser.insertInt))
  def executeInsertInts()(implicit connection: Connection): List[Int] = InsertionStatementPreparer(connection).execute(_.asList(RowParser.insertInt))
  def executeInsertLong()(implicit connection: Connection): Long = InsertionStatementPreparer(connection).execute(_.asSingle(RowParser.insertLong))
  def executeInsertLongs()(implicit connection: Connection): List[Long] = InsertionStatementPreparer(connection).execute(_.asList(RowParser.insertLong))

  def executeInsertSingle[U](parser: RowParser[U])(implicit connection: Connection): U = InsertionStatementPreparer(connection).execute(_.asSingle(parser))
  def executeInsertCollection[U, T[_]](parser: RowParser[U])(implicit cbf: CanBuildFrom[T[U], U, T[U]], connection: Connection): T[U] = InsertionStatementPreparer(connection).execute(_.asCollection(parser))

  def asSingle[A](parser: RowParser[A])(implicit connection: Connection): A = NormalStatementPreparer(connection).execute(_.asSingle(parser))
  def asSingleOption[A](parser: RowParser[A])(implicit connection: Connection): Option[A] = NormalStatementPreparer(connection).execute(_.asSingleOption(parser))
  def asSet[A](parser: RowParser[A])(implicit connection: Connection): Set[A] = NormalStatementPreparer(connection).execute(_.asSet(parser))
  def asSeq[A](parser: RowParser[A])(implicit connection: Connection): Seq[A] = NormalStatementPreparer(connection).execute(_.asSeq(parser))
  def asIterable[A](parser: RowParser[A])(implicit connection: Connection): Iterable[A] = NormalStatementPreparer(connection).execute(_.asIterable(parser))
  def asList[A](parser: RowParser[A])(implicit connection: Connection): List[A] = NormalStatementPreparer(connection).execute(_.asList(parser))
  def asMap[U, V](parser: RowParser[(U, V)])(implicit connection: Connection): Map[U, V] = NormalStatementPreparer(connection).execute(_.asMap(parser))
  def asScalar[A]()(implicit connection: Connection): A = NormalStatementPreparer(connection).execute(_.asScalar[A]())
  def asScalarOption[A]()(implicit connection: Connection): Option[A] = NormalStatementPreparer(connection).execute(_.asScalarOption[A]())
  def asCollection[U, T[_]](parser: RowParser[U])(implicit cbf: CanBuildFrom[T[U], U, T[U]], connection: Connection): T[U] = NormalStatementPreparer(connection).execute(_.asCollection(parser))
  def asPairCollection[U, V, T[_, _]](parser: RowParser[(U, V)])(implicit cbf: CanBuildFrom[T[U, V], (U, V), T[U, V]], connection: Connection): T[U, V] = NormalStatementPreparer(connection).execute(_.asPairCollection(parser))
  
  /**
   * The asIterator method returns an Iterator that will stream data out of the database.
   * This avoids an OutOfMemoryError when dealing with large datasets
   * @param parser the RowParser to parse rows with
   * @param fetchSize the number of rows to fetch at a time, defaults to 100. If the JDBC Driver
   * is MySQL, the fetchSize will always default to Int.MinValue, as MySQL's JDBC implementation
   * ignores all other fetchSize values and only streams if fetchSize is Int.MinValue
   */
  def asIterator[A](parser: RowParser[A], fetchSize: Int = 100)(implicit connection: Connection): Iterator[A] = {
    val prepared = StreamedStatementPreparer(connection, fetchSize)
    prepared.execute(RowIterator(parser, prepared.stmt, _))
  }

  private trait StatementPreparer {

    protected val (parsedQuery, parsedParams) = SqlStatementParser.parse(query, listParams)
    val stmt = prepare()

    protected def prepare(): PreparedStatement
    protected def results(): ResultSet
    def connection: Connection

    /**
     * Execute the statement and close all resources
     * @param callback the function to call on the results of the query
     * @return whatever the callback returns
     */
    def execute[A](callback: (SqlResult) => A): A = {
      try {
        val resultSet = results()
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

    /**
     * Execute the query and close
     * @return if the query succeeded or not
     */
    def execute(): Boolean = {
      try {
        stmt.execute()
      }
      finally {
        stmt.close()
      }
    }

    /**
     * Execute the query and close
     * @return the number of rows affected by the query
     */
    def executeUpdate(): Int = {
      try {
        stmt.executeUpdate()
      }
      finally {
        stmt.close()
      }
    }

    /**
     * Apply all the functions that put in parameters
     * @param stmt the PreparedStatement to apply the functions to
     * @return the PreparedStatement
     */
    protected def applyParams(stmt: PreparedStatement): PreparedStatement = {
      val sqlStmt = new SqlStatement(stmt, parsedParams, listParams)
      params.reverse.foreach { f =>
        f(sqlStmt)
      }
      stmt
    }
  }

  private case class NormalStatementPreparer(connection: Connection) extends StatementPreparer {
    /**
     * Get a PreparedStatement from this query with inserted parameters
     * @return the PreparedStatement
     */
    protected override def prepare(): PreparedStatement = {
      val stmt = connection.prepareStatement(parsedQuery)
      applyParams(stmt)
      stmt
    }
    
    /**
     * Get the results of excutioning this statement
     * @return the resulting ResultSet
     */
    protected override def results(): ResultSet = {
      stmt.executeQuery()
    }
  }

  private case class InsertionStatementPreparer(connection: Connection) extends StatementPreparer {
    /**
     * Get a PreparedStatement from this query that will return generated keys
     * @return the PreparedStatement
     */
    protected override def prepare(): PreparedStatement = {
      val stmt = connection.prepareStatement(parsedQuery, Statement.RETURN_GENERATED_KEYS)
      applyParams(stmt)
      stmt
    }

    /**
     * Get the results of executing this insertion statement
     * @return the ResultSet
     */
    protected override def results(): ResultSet = {
      stmt.executeUpdate()
      stmt.getGeneratedKeys()
    }
  }

  private case class StreamedStatementPreparer(connection: Connection, fetchSize: Int) extends StatementPreparer {
    /**
     * Get a PreparedStatement from this query that will stream the resulting rows
     * @return the PreparedStatement
     */
    protected override def prepare(): PreparedStatement = {
      val stmt = connection.prepareStatement(
        parsedQuery,
        ResultSet.TYPE_FORWARD_ONLY,
        ResultSet.CONCUR_READ_ONLY
      )
      val driver = connection.getMetaData().getDriverName()
      if (driver.toLowerCase.contains("mysql")) {
        stmt.setFetchSize(Int.MinValue)
      }
      else {
        stmt.setFetchSize(fetchSize)
      }
      applyParams(stmt)
      stmt
    }
    
    /**
     * Override the default execute method so that it does not close the resources
     * @param callback the function to call on the results of the query
     * @return whatever the callback returns
     */
    override def execute[A](callback: (SqlResult) => A): A = {
      callback(SqlResult(results()))
    }

    /**
     * Get the results of executing this statement with a streaming ResultSet
     * @return the ResultSet
     */
    protected override def results(): ResultSet = {
      stmt.executeQuery()
    }
  }
}
