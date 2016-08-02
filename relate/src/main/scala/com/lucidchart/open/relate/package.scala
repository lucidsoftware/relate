package com.lucidchart.open

import java.sql.Connection
import java.sql.Date
import java.util.UUID

/**
 * Relate API
 *
 * Use the SQL method to start an SQL query
 *
 * {{{
 * import com.lucidchart.open.relate._
 *
 * SQL("Select 1")
 * }}}
 */
package object relate {

  /**
   * Create a SQL query with the provided statement
   * @param stmt the SQL statement
   *
   * {{{
   * val query = SQL("SELECT * FROM users")
   * }}}
   */
  def SQL(stmt: String): ExpandableQuery = Relate.sql(stmt)

}