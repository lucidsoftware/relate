package com.lucidchart.open.relate

import java.sql.{Connection, PreparedStatement}

/** A query object that has not had parameter values substituted in */
case class SqlQuery(
  connection: Connection,
  query: String,
  args: List[String]
) {
  
  /** The smart prepared statement that is used to fill in parameters */
  val stmt = new SqlStatement(connection.prepareStatement(query), query, args)

  /**
   * Put in values for parameters in the query
   * @param f a function that takes a SqlStatement and sets parameter values using its methods
   * @return this SqlQuery
   */
  def on(f: SqlStatement => Unit): SqlQuery = {
    f(stmt)
    this
  }

  /**
   * Execute a statement
   */
  def execute(): Boolean = {
    stmt.execute()
  }

  /**
   * Execute an update
   */
  def executeUpdate(): Int = {
    stmt.executeUpdate()
  }

  /**
   * Execute a query
   */
  def executeQuery(): SqlResult = {
    stmt.executeQuery()
  }

  /**
   * Execute an insert
   */
  def executeInsert(): SqlResult = {
    stmt.executeInsert()
  }

}
