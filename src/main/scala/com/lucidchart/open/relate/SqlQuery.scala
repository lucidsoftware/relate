package com.lucidchart.open.relate

import java.sql.{Connection, PreparedStatement, ResultSet}
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable

/**
 * Sql is a trait for basic SQL queries.
 *
 * It provides methods for parameter insertion and query execution.
 */
trait Sql {
  self =>

  protected val parsedQuery: String
  protected def applyParams(stmt: PreparedStatement): Unit

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
  def executeInsertInt()(implicit connection: Connection): Int = insertionStatement.execute(RowParser.insertInt)

  /**
   * Execute the query and get the auto-incremented keys as a List of Ints
   * @param connection the connection to use when executing the query
   * @return the auto-incremented keys as a List of Ints
   */
  def executeInsertInts()(implicit connection: Connection): List[Int] = insertionStatement.execute(Parser.listOf(RowParser.insertInt _))

  /**
   * Execute the query and get the auto-incremented key as a Long
   * @param connection the connection to use when executing the query
   * @return the auto-incremented key as a Long
   */
  def executeInsertLong()(implicit connection: Connection): Long = {
    insertionStatement.executeInsertLong()
  }

  /**
   * Execute the query and get the auto-incremented keys as a a List of Longs
   * @param connection the connection to use when executing the query
   * @return the auto-incremented keys as a a List of Longs
   */
  def executeInsertLongs()(implicit connection: Connection): List[Long] = insertionStatement.execute(Parser.listOf(RowParser.insertLong _))

  /**
   * Execute the query and get the auto-incremented key using a RowParser. Provided for the case
   * that a primary key is not an Int or BigInt
   * @param parser the RowParser that can parse the returned key
   * @param connection the connection to use when executing the query
   * @return the auto-incremented key
   */
  def executeInsertSingle[U](parser: SqlRow => U)(implicit connection: Connection): U = insertionStatement.execute(parser)

  /**
   * Execute the query and get the auto-incremented keys using a RowParser. Provided for the case
   * that a primary key is not an Int or BigInt
   * @param parser the RowParser that can parse the returned keys
   * @param connection the connection to use when executing the query
   * @return the auto-incremented keys
   */
  //def executeInsertCollection[U, T[_]](parser: SqlRow => U)(implicit cbf: CanBuildFrom[T[U], U, T[U]], connection: Connection): T[U] = insertionStatement.execute(_.asCollection(parser))

  def as[A: Parser](implicit conn: Connection): A = normalStatement.execute { row =>
    implicitly[Parser[A]].doParse(row)
  }

  def as[A](parser: SqlRow => A)(implicit conn: Connection): A = normalStatement.execute { row =>
    Parser(parser).doParse(row)
  }

  def asMap[A: Parser](keyColumn: String)(implicit conn: Connection): Map[String, A] =
    normalStatement.execute { row =>
      as[Map[String, A]](Parser.pairCollection[String, A, Map](
        Parser[String](row => row.string(keyColumn)),
        implicitly[Parser[A]],
        implicitly[CanBuildFrom[Map[String, A], (String, A), Map[String, A]]]
      ), conn)
    }

  /**
   * Execute this query and get back the result as a single value. Assumes that there is only one
   * row and one value in the result set.
   * @param connection the connection to use when executing the query
   * @return the results as a single value
   */
  def asScalar[A: Parser: Numeric]()(implicit connection: Connection): A = normalStatement.execute(Parser.scalar[A])

  /**
   * Execute this query and get back the result as an optional single value. Assumes that there is
   * only one row and one value in the result set.
   * @param parser the RowParser to use when parsing the result set
   * @param connection the connection to use when executing the query
   * @return the results as an optional single value
   */
  def asScalarOption[A: Parser: Numeric]()(implicit connection: Connection): Option[A] = normalStatement.execute(Parser.scalarOption[A])

  /**
   * The asIterator method returns an Iterator that will stream data out of the database.
   * This avoids an OutOfMemoryError when dealing with large datasets. Bear in mind that many
   * JDBC implementations will not allow additional queries to the connection before all records
   * in the Iterator have been retrieved.
   * @param fetchSize the number of rows to fetch at a time, defaults to 100. If the JDBC Driver
   * is MySQL, the fetchSize will always default to Int.MinValue, as MySQL's JDBC implementation
   * ignores all other fetchSize values and only streams if fetchSize is Int.MinValue
   */
  def asIterator[A: Parser](fetchSize: Int = 100)(implicit connection: Connection): Iterator[A] = {
    val prepared = streamedStatement(fetchSize)
    prepared.execute(RowIterator(implicitly[Parser[A]], prepared.stmt, _))
  }
}
