package com.lucidchart.open.relate

import java.sql.{Connection, PreparedStatement, Statement}
import scala.collection.mutable

/** A query object that can be expanded */
private[relate] case class ExpandableQuery(
  query: String,
  listParams: mutable.Map[String, ListParam] = mutable.Map[String, ListParam]()
) extends Sql with Expandable {

  val params = Nil

  /** 
   * The copy method used by the Sql Trait
   * Returns a SqlQuery object so that expansion can only occur before the 'on' method
   */
  protected def getCopy(p: List[SqlStatement => Unit]): SqlQuery = SqlQuery(query, p, listParams)
}


/** A query object that has not had parameter values substituted in */
private[relate] case class SqlQuery(
  query: String,
  params: List[SqlStatement => Unit] = Nil,
  listParams: mutable.Map[String, ListParam] = mutable.Map[String, ListParam]()
) extends Sql {

  /**
   * Copy method for the Sql trait
   */
  protected def getCopy(p: List[SqlStatement => Unit]): SqlQuery = copy(params = p)

}

/** The base class for all list type parameters. Contains a name and a count (total number of 
inserted params) */
sealed trait ListParam {
  val name: String
  val count: Int
  val charCount: Int
}

/** ListParam type that represents a comma separated list of parameters */
private[relate] case class CommaSeparated(
  name: String,
  count: Int,
  charCount: Int
) extends ListParam

/** ListParam type that represents a comma separated list of tuples */
private[relate] case class Tupled(
  name: String,
  count: Int,
  charCount: Int,
  params: Map[String, Int],
  numTuples: Int,
  tupleSize: Int
) extends ListParam

/** A trait for SQL queries that can be expanded */
sealed trait Expandable extends Sql {

  /** The names of list params mapped to their size */
  val listParams: mutable.Map[String, ListParam]

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
   * WARNING: modifies this Expandable in place
   * @param name the identifier for the parameter
   * @param count the count of parameters in the list
   */
  def commaSeparated(name: String, count: Int) {
    listParams(name) = CommaSeparated(name, count, count * 2)
  }

  /**
   * Replace the provided identifier with a comma separated list of tuples
   * WARNING: modifies this Expandable in place
   * @param name the identifier for the tuples
   * @param columns a list of the column names in the order they should be inserted into
   * the tuples
   * @param count the number of tuples to insert
   */
  def tupled(name: String, columns: Seq[String], count: Int) {
    val namesToIndexes = columns.zipWithIndex.toMap
    listParams(name) = Tupled(
      name,
      count * columns.size,
      count * 3 + count * columns.size * 2,
      namesToIndexes,
      count,
      columns.size
    )
  }

}

/** A trait for queries */
sealed trait Sql {

  val query: String
  val params: List[SqlStatement => Unit]
  val listParams: mutable.Map[String, ListParam]

  /**
   * Classes that inherit the Sql trait will have to implement a method to copy
   * themselves given just a different set of parameters. HINT: Use a case class!
   */
  protected def getCopy(params: List[SqlStatement => Unit]): Sql

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
  private def prepareSqlStatement(connection: Connection, getGeneratedKeys: Boolean = false): 
    SqlStatement = {
    val (parsedQuery, parsedParams) = SqlStatementParser.parse(query, listParams)
    val stmt = if (getGeneratedKeys) {
      connection.prepareStatement(parsedQuery, Statement.RETURN_GENERATED_KEYS)
    }
    else {
      connection.prepareStatement(parsedQuery)
    }
    applyParams(new SqlStatement(stmt, parsedParams, listParams))
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
//   tupled("values", List("id", "name", "created"), records.size)
// }.onTuples(records) { record => implicit something =>
//   int("id", record.id)
//   string("name", record.name)
//   timestamp("created", new Date())
// }

//the implicit is a "record" object, which then knows which method to call on the
//statement based upon its own index
