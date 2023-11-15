package com.lucidchart.relate

import java.sql.{Connection, PreparedStatement, ResultSet}
import scala.collection.compat._
import scala.language.higherKinds

/**
 * CollectionsSql is a trait for collection SQL queries.
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
 * }.asCollection(RowParser { row =>
 *   User(row.long("id"), row.string("name"))
 * })
 * }}}
 */
trait CollectionsSql { self: Sql =>

  /**
   * Execute the query and get the auto-incremented keys using a RowParser. Provided for the case that a primary key is
   * not an Int or BigInt
   * @param parser
   *   the RowParser that can parse the returned keys
   * @param connection
   *   the connection to use when executing the query
   * @return
   *   the auto-incremented keys
   */
  def executeInsertCollection[U, T[_]](
    parser: SqlRow => U
  )(implicit factory: Factory[U, T[U]], connection: Connection): T[U] =
    insertionStatement.execute(_.asCollection(parser))

  /**
   * Execute this query and get back the result as an arbitrary collection of records
   * @param parser
   *   the RowParser to use when parsing the result set
   * @param connection
   *   the connection to use when executing the query
   * @return
   *   the results as an arbitrary collection of records
   */
  def asCollection[U, T[_]](parser: SqlRow => U)(implicit factory: Factory[U, T[U]], connection: Connection): T[U] =
    normalStatement.execute(_.asCollection(parser))
  def asCollection[U: RowParser, T[_]]()(implicit factory: Factory[U, T[U]], connection: Connection): T[U] =
    normalStatement.execute(_.asCollection[U, T])

  /**
   * Execute this query and get back the result as an arbitrary collection of key value pairs
   * @param parser
   *   the RowParser to use when parsing the result set
   * @param connection
   *   the connection to use when executing the query
   * @return
   *   the results as an arbitrary collection of key value pairs
   */
  def asPairCollection[U, V, T[_, _]](
    parser: SqlRow => (U, V)
  )(implicit factory: Factory[(U, V), T[U, V]], connection: Connection): T[U, V] =
    normalStatement.execute(_.asPairCollection(parser))
  def asPairCollection[U, V, T[_, _]]()(implicit
    factory: Factory[(U, V), T[U, V]],
    connection: Connection,
    p: RowParser[(U, V)]
  ): T[U, V] = normalStatement.execute(_.asPairCollection[U, V, T])
}
