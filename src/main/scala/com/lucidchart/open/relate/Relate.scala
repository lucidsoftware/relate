package com.lucidchart.open.relate

import java.sql.Connection

object Relate {

  /**
   * Create a SqlQuery object from a SQL statement
   * @param stmt the SQL statement
   * @return the corresponding SqlQuery object
   */
  private[relate] def sql(stmt: String): SqlQuery = {
    val (query, args) = SqlStatementParser.parse(stmt)
    SqlQuery(query, args)
  }

}