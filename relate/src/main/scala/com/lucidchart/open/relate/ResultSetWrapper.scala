package com.lucidchart.open.relate

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

  private[relate] def asRow: SqlRow = SqlRow(resultSet)
  private[relate] def asResult: SqlResult = SqlResult(resultSet)
}
