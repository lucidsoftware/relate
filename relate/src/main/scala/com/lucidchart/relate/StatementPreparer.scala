package com.lucidchart.relate

import java.sql._

private[relate] sealed trait StatementPreparer {

  val stmt = prepare()

  protected def prepare(): PreparedStatement
  def results(): ResultSet
  def connection: Connection

  /**
   * Execute the statement and close all resources
   * @param callback
   *   the function to call on the results of the query
   * @return
   *   whatever the callback returns
   */
  def execute[A](callback: (SqlResult) => A): A = {
    try {
      val resultSet = results()
      try {
        callback(SqlResult(resultSet))
      } finally {
        resultSet.close()
      }
    } finally {
      stmt.close()
    }
  }

  /**
   * Execute the query and close
   * @return
   *   true if the first result is a ResultSet object; false if the first result is an update count or there is no
   *   result
   */
  def execute(): Boolean = {
    try {
      stmt.execute()
    } finally {
      stmt.close()
    }
  }

  /**
   * Execute the query and close
   * @return
   *   the number of rows affected by the query
   */
  def executeUpdate(): Int = {
    try {
      stmt.executeUpdate()
    } finally {
      stmt.close()
    }
  }

}

private[relate] trait BaseStatementPreparer extends StatementPreparer {
  protected def applyParams(stmt: PreparedStatement)
  protected def parsedQuery: String
  protected def timeout: Option[Int] = None

  protected def setTimeout(stmt: PreparedStatement): Unit = for {
    seconds <- timeout
    stmt <- Option(stmt)
  } yield stmt.setQueryTimeout(seconds)
}

private[relate] trait NormalStatementPreparer extends BaseStatementPreparer {

  /**
   * Get a PreparedStatement from this query with inserted parameters
   * @return
   *   the PreparedStatement
   */
  protected override def prepare(): PreparedStatement = {
    val stmt = connection.prepareStatement(parsedQuery)
    setTimeout(stmt)
    applyParams(stmt)
    stmt
  }

  /**
   * Get the results of excutioning this statement
   * @return
   *   the resulting ResultSet
   */
  override def results(): ResultSet = {
    stmt.executeQuery()
  }
}

private[relate] trait InsertionStatementPreparer extends BaseStatementPreparer {

  /**
   * Get a PreparedStatement from this query that will return generated keys
   * @return
   *   the PreparedStatement
   */
  protected override def prepare(): PreparedStatement = {
    val stmt = connection.prepareStatement(parsedQuery, Statement.RETURN_GENERATED_KEYS)
    setTimeout(stmt)
    applyParams(stmt)
    stmt
  }

  /**
   * Get the results of executing this insertion statement
   * @return
   *   the ResultSet
   */
  override def results(): ResultSet = {
    stmt.executeUpdate()
    stmt.getGeneratedKeys()
  }
}

private[relate] trait StreamedStatementPreparer extends BaseStatementPreparer {
  protected val fetchSize: Int

  /**
   * Get a PreparedStatement from this query that will stream the resulting rows
   * @return
   *   the PreparedStatement
   */
  protected override def prepare(): PreparedStatement = {
    val stmt = connection.prepareStatement(
      parsedQuery,
      ResultSet.TYPE_FORWARD_ONLY,
      ResultSet.CONCUR_READ_ONLY
    )
    setTimeout(stmt)
    val driver = connection.getMetaData().getDriverName()
    if (driver.toLowerCase.contains("mysql")) {
      stmt.setFetchSize(Int.MinValue)
    } else {
      stmt.setFetchSize(fetchSize)
    }
    applyParams(stmt)
    stmt
  }

  /**
   * Override the default execute method so that it does not close the resources
   * @param callback
   *   the function to call on the results of the query
   * @return
   *   whatever the callback returns
   */
  override def execute[A](callback: (SqlResult) => A): A = {
    callback(SqlResult(results()))
  }

  /**
   * Get the results of executing this statement with a streaming ResultSet
   * @return
   *   the ResultSet
   */
  override def results(): ResultSet = {
    stmt.executeQuery()
  }
}
