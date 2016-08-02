package com.lucidchart.open.relate

import java.sql.SQLException

trait ResultSetWrapper {
  val resultSet: java.sql.ResultSet

  /**
    * Determine if the last value extracted from the result set was null
    * @return whether the last value was null
    */
  def wasNull(): Boolean = resultSet.wasNull()

  def next(): Boolean = resultSet.next()

  def withResultSet[A](f: (java.sql.ResultSet) => A) = {
    try {
      f(resultSet)
    }
    finally {
      resultSet.close()
    }
  }

  /**
    * Determine if the result set contains the given column name
    * @param column the column name to check
    * @return whether or not the result set contains that column name
    */
  def hasColumn(column: String): Boolean = {
    try {
      resultSet.findColumn(column)
      true
    }
    catch {
      case e: SQLException => false
    }
  }

  private[relate] def asRow: SqlRow = SqlRow(resultSet)
  private[relate] def asResult: SqlResult = SqlResult(resultSet)
}
