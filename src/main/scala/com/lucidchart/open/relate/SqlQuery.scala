package com.lucidchart.open.relate

import java.sql.{Connection, PreparedStatement, Statement}

/** A query object that has not had parameter values substituted in */
case class SqlQuery(
  query: String,
  args: Map[String, Int],
  params: List[SqlStatement=>Unit] = Nil
) {
  /**
   * Put in values for parameters in the query
   * @param f a function that takes a SqlStatement and sets parameter values using its methods
   * @return this SqlQuery
   */
  def on(f: SqlStatement => Unit): SqlQuery = {
    copy(params=(f +: params))
  }

  /**
   * Get a completed SqlStatement objecct to execute
   * @param getGeneratedKeys whether or not to get the generated keys
   * @return the prepared SqlStatement
   */
  private def prepareSqlStatement(connection: Connection, getGeneratedKeys: Boolean = false): SqlStatement = {
    val stmt = if (getGeneratedKeys) connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
      else connection.prepareStatement(query)
    applyParams(new SqlStatement(stmt, args))
  }

  /**
   * Apply all the functions that put in parameters
   * @param stmt the SqlStatement to apply the functions to
   * @param the prepared SqlStatement
   */
  private def applyParams(stmt: SqlStatement): SqlStatement = {
    params.reverse.foreach { f =>
      f(stmt)
    }
    stmt
  }

  /**
   * Execute a statement
   */
  def execute()(implicit connection: Connection): Boolean = {
    prepareSqlStatement(connection).execute()
  }

  /**
   * Execute an update
   */
  def executeUpdate()(implicit connection: Connection): Int = {
    prepareSqlStatement(connection).executeUpdate()
  }

  /**
   * Execute a query
   */
  def executeQuery()(implicit connection: Connection): SqlResult = {
    prepareSqlStatement(connection).executeQuery()
  }

  /**
   * Execute an insert
   */
  def executeInsert()(implicit connection: Connection): SqlResult = {
    prepareSqlStatement(connection, true).executeInsert()
  }

}
