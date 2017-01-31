package com.lucidchart.relate

import java.sql.{Connection, PreparedStatement, ResultSet}
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable
import scala.language.higherKinds

/**
 * Sql is a trait for basic SQL queries.
 *
 * It provides methods for parameter insertion and query execution.
 * {{{
 * import com.lucidchart.relate._
 * import com.lucidchart.relate.Query._
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
  def executeInsertSingle[U](parser: SqlRow => U)(implicit connection: Connection): U = insertionStatement.execute(_.asSingle(parser))

  /**
   * Execute the query and get the auto-incremented keys using a RowParser. Provided for the case
   * that a primary key is not an Int or BigInt
   * @param parser the RowParser that can parse the returned keys
   * @param connection the connection to use when executing the query
   * @return the auto-incremented keys
   */
  def executeInsertCollection[U, T[_]](parser: SqlRow => U)(implicit cbf: CanBuildFrom[T[U], U, T[U]], connection: Connection): T[U] = insertionStatement.execute(_.asCollection(parser))

  def as[A: RowParser]()(implicit connection: Connection): A = normalStatement.execute(_.as[A])

  /**
   * Execute this query and get back the result as a single record
   * @param parser the RowParser to use when parsing the result set
   * @param connection the connection to use when executing the query
   * @return the results as a single record
   */
  def asSingle[A](parser: SqlRow => A)(implicit connection: Connection): A = normalStatement.execute(_.asSingle(parser))
  def asSingle[A: RowParser]()(implicit connection: Connection): A = normalStatement.execute(_.asSingle[A])

  /**
   * Execute this query and get back the result as an optional single record
   * @param parser the RowParser to use when parsing the result set
   * @param connection the connection to use when executing the query
   * @return the results as an optional single record
   */
  def asSingleOption[A](parser: SqlRow => A)(implicit connection: Connection): Option[A] = normalStatement.execute(_.asSingleOption(parser))
  def asSingleOption[A: RowParser]()(implicit connection: Connection): Option[A] = normalStatement.execute(_.asSingleOption[A])

  /**
   * Execute this query and get back the result as a Set of records
   * @param parser the RowParser to use when parsing the result set
   * @param connection the connection to use when executing the query
   * @return the results as a Set of records
   */
  def asSet[A](parser: SqlRow => A)(implicit connection: Connection): Set[A] = normalStatement.execute(_.asSet(parser))
  def asSet[A: RowParser]()(implicit connection: Connection): Set[A] = normalStatement.execute(_.asSet[A])

  /**
   * Execute this query and get back the result as a sequence of records
   * @param parser the RowParser to use when parsing the result set
   * @param connection the connection to use when executing the query
   * @return the results as a sequence of records
   */
  def asSeq[A](parser: SqlRow => A)(implicit connection: Connection): Seq[A] = normalStatement.execute(_.asSeq(parser))
  def asSeq[A: RowParser]()(implicit connection: Connection): Seq[A] = normalStatement.execute(_.asSeq[A])

  /**
   * Execute this query and get back the result as an iterable of records
   * @param parser the RowParser to use when parsing the result set
   * @param connection the connection to use when executing the query
   * @return the results as an iterable of records
   */
  def asIterable[A](parser: SqlRow => A)(implicit connection: Connection): Iterable[A] = normalStatement.execute(_.asIterable(parser))
  def asIterable[A: RowParser]()(implicit connection: Connection): Iterable[A] = normalStatement.execute(_.asIterable[A])

  /**
   * Execute this query and get back the result as a List of records
   * @param parser the RowParser to use when parsing the result set
   * @param connection the connection to use when executing the query
   * @return the results as a List of records
   */
  def asList[A](parser: SqlRow => A)(implicit connection: Connection): List[A] = normalStatement.execute(_.asList(parser))
  def asList[A: RowParser]()(implicit connection: Connection): List[A] = normalStatement.execute(_.asList[A])

  /**
   * Execute this query and get back the result as a Map of records
   * @param parser the RowParser to use when parsing the result set. The RowParser should return a Tuple
   * of size 2 containing the key and value
   * @param connection the connection to use when executing the query
   * @return the results as a Map of records
   */
  def asMap[U, V](parser: SqlRow => (U, V))(implicit connection: Connection): Map[U, V] = normalStatement.execute(_.asMap(parser))
  def asMap[U, V]()(implicit connection: Connection, p: RowParser[(U, V)]): Map[U, V] = normalStatement.execute(_.asMap[U, V])

  def asMultiMap[U, V](parser: SqlRow => (U, V))(implicit connection: Connection): Map[U, Set[V]] = normalStatement.execute(_.asMultiMap(parser))
  def asMultiMap[U, V]()(implicit connection: Connection, p: RowParser[(U, V)]): Map[U, Set[V]] = normalStatement.execute(_.asMultiMap[U, V])

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
  def asCollection[U, T[_]](parser: SqlRow => U)(implicit cbf: CanBuildFrom[T[U], U, T[U]], connection: Connection): T[U] = normalStatement.execute(_.asCollection(parser))
  def asCollection[U: RowParser, T[_]]()(implicit cbf: CanBuildFrom[T[U], U, T[U]], connection: Connection): T[U] = normalStatement.execute(_.asCollection[U, T])

  /**
   * Execute this query and get back the result as an arbitrary collection of key value pairs
   * @param parser the RowParser to use when parsing the result set
   * @param connection the connection to use when executing the query
   * @return the results as an arbitrary collection of key value pairs
   */
  def asPairCollection[U, V, T[_, _]](parser: SqlRow => (U, V))(implicit cbf: CanBuildFrom[T[U, V], (U, V), T[U, V]], connection: Connection): T[U, V] = normalStatement.execute(_.asPairCollection(parser))
  def asPairCollection[U, V, T[_, _]]()(implicit cbf: CanBuildFrom[T[U, V], (U, V), T[U, V]], connection: Connection, p: RowParser[(U, V)]): T[U, V] = normalStatement.execute(_.asPairCollection[U, V, T])

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
  def asIterator[A](parser: SqlRow => A, fetchSize: Int = 100)(implicit connection: Connection): Iterator[A] = {
    val prepared = streamedStatement(fetchSize)
    prepared.execute(RowIterator(parser, prepared.stmt, _))
  }
}
