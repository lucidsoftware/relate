package com.lucidchart.open.relate

import java.sql.{Connection, PreparedStatement, ResultSet, Statement}

private[relate] sealed trait StatementPreparer {

  def queryParams: QueryParams

  protected val (parsedQuery, parsedParams) = SqlStatementParser.parse(queryParams.query, queryParams.listParams)
  val stmt = prepare()

  protected def prepare(): PreparedStatement
  def results(): ResultSet
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
    val sqlStmt = new SqlStatement(stmt, parsedParams, queryParams.listParams)
    queryParams.params.reverse.foreach { f =>
      f(sqlStmt)
    }
    stmt
  }
}

private[relate] case class NormalStatementPreparer(queryParams: QueryParams, connection: Connection) extends StatementPreparer {
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
  override def results(): ResultSet = {
    stmt.executeQuery()
  }
}

private[relate] case class InsertionStatementPreparer(queryParams: QueryParams, connection: Connection) extends StatementPreparer {
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
  override def results(): ResultSet = {
    stmt.executeUpdate()
    stmt.getGeneratedKeys()
  }
}

private[relate] case class StreamedStatementPreparer(queryParams: QueryParams, connection: Connection, fetchSize: Int) extends StatementPreparer {
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
  override def results(): ResultSet = {
    stmt.executeQuery()
  }
}