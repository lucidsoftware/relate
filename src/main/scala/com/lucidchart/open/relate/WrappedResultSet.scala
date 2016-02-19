package com.lucidchart.open.relate

import java.sql.ResultSetMetaData


trait WrappedResultSet {
  val resultSet: java.sql.ResultSet

  /**
   * Get the number of the row the SqlResult is currently on
   * @return the current row number
   */
  def getRow(): Int = resultSet.getRow()

  /**
    * Get the metadata for the java.sql.ResultSet that underlies this SqlResult
    * @return the metadata
    */
  def getMetaData(): ResultSetMetaData = resultSet.getMetaData()

  /**
    * Determine if the last value extracted from the result set was null
    * @return whether the last value was null
    */
  def wasNull(): Boolean = resultSet.wasNull()
  private[relate] def next(): Boolean = resultSet.next()

  protected[relate] def withResultSet[A](f: (java.sql.ResultSet) => A) = {
    try {
      f(resultSet)
    }
    finally {
      resultSet.close()
    }
  }
}