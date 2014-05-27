package com.lucidchart.open.relate

import java.sql.{Connection, PreparedStatement, Statement}
import scala.collection.mutable

/** A query object that can be expanded */
case class ExpandableQuery(
  query: String,
  listParams: mutable.Map[String, Int] = mutable.Map[String, Int]()
) extends Sql with Expandable {

  val params = Nil

  /** 
   * The copy method used by the Sql Trait
   * Returns a SqlQuery object so that expansion can only occur before the 'on' method
   */
  def getCopy(p: List[SqlStatement => Unit]): SqlQuery = SqlQuery(query, p, listParams)
}


/** A query object that has not had parameter values substituted in */
case class SqlQuery(
  query: String,
  params: List[SqlStatement => Unit] = Nil,
  listParams: mutable.Map[String, Int] = mutable.Map[String, Int]()
) extends Sql {

  /**
   * Copy method for the Sql trait
   */
  def getCopy(p: List[SqlStatement => Unit]): SqlQuery = copy(params = p)

}


/** A trait for SQL queries that can be expanded */
trait Expandable extends Sql {

  /** The names of list params mapped to their size */
  val listParams: mutable.Map[String, Int]

  /**
   * Expand out the query by turning an Iterable into several parameters
   * @param a function that will operate by calling functions on this Expandable instance
   * @return a copy of this Expandable with the query expanded
   */
  def expand(f: Expandable => Unit): Expandable = {
    f(this)
    this
  }

  /**
   * Replace the provided identifier with a comma separated list of parameters
   * WARNING: modifies this Expandable's query in place
   * @param name the identifier for the parameter
   * @param count the count of parameters in the list
   */
  def commaSeparated(name: String, count: Int) {
    listParams(name) = count
  }

}

/** A trait for queries */
trait Sql {

  val query: String
  val params: List[SqlStatement => Unit]
  val listParams: mutable.Map[String, Int]

  /**
   * Classes that inherit the Sql trait will have to implement a method to copy
   * themselves given just a different set of parameters. HINT: Use a case class!
   */
  def getCopy(params: List[SqlStatement => Unit]): Sql

  /**
   * Put in values for parameters in the query
   * @param f a function that takes a SqlStatement and sets parameter values using its methods
   * @return a copy of this Sql with the new params
   */
  def on(f: SqlStatement => Unit): Sql = {
    getCopy(params = (f +: params))
  }

  /**
   * Get a completed SqlStatement objecct to execute
   * @param getGeneratedKeys whether or not to get the generated keys
   * @return the prepared SqlStatement
   */
  private def prepareSqlStatement(connection: Connection, getGeneratedKeys: Boolean = false): SqlStatement = {
    val (parsedQuery, parsedParams) = SqlStatementParser.parse(query, listParams)
    val stmt = if (getGeneratedKeys) connection.prepareStatement(parsedQuery, Statement.RETURN_GENERATED_KEYS)
      else connection.prepareStatement(parsedQuery)
    applyParams(new SqlStatement(stmt, parsedParams))
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

// SQL("""insert into users (id, name, created) values {values}""").expand { implicit something =>
//   // better name exists
//   forInsert("values", List("id", "name", "created"), records.size)
// }.onRecords(records) { record => implicit something =>
//   int("id", record.id)
//   string("name", record.name)
//   timestamp("created", new Date())
// }

