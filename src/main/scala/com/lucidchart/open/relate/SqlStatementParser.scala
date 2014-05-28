package com.lucidchart.open.relate

import scala.collection.mutable
import scala.collection.Map

object SqlStatementParser {

  /**
   * Parse a SQL statement into parameters and replace all parameters with ?
   * @param stmt the statement to process
   * @param listParams a mapping of list parameter names to their sizes
   * @return a tuple containing the revised SQL statement and the parameter names to their index
   */
  def parse(stmt: String, listParams: Map[String, ListParam] = Map[String, ListParam]()):
    (String, Map[String, List[Int]]) = {
    
    val query = new StringBuilder(stmt.length + 2 * listParams.values.foldLeft(0) (_ + _.count))
    val param = new StringBuilder(100)
    
    var inParam = false
    val params = mutable.Map[String, List[Int]]()
    var index = 1
    var i = 0
    val chars = stmt.toCharArray
    while (i < chars.size) {
      val c = chars(i)
      if (!inParam) {
        if (c == '{') {
          inParam = true
        }
        else {
          query.append(c)
        }
      }
      else {
        if (c == '}') {
          val name = param.toString

          params(name) = if (params contains name) params(name) :+ index else List(index)
          if (!listParams.isEmpty && listParams.contains(name)) {
            listParams(name) match {
              case x: CommaSeparated => {
                insertCommaSeparated(x.count, query)
                index += x.count
              }
              case x: Tupled => {
                insertTuples(x.numTuples, x.tupleSize, query)
                index += x.count
              }
            }
          }
          else {
            query.append('?')
            index += 1
          }
          
          inParam = false
          param.clear
        }
        else {
          param.append(c)
        }
      }

      i += 1
    }

    (query.toString, params.toMap)
  }

  /**
   * Insert a comma separated list of comma separated ? into the query
   * @param count the number of parameters in the list
   * @param query the current query in a StringBuilder
   */
  private def insertCommaSeparated(count: Int, query: StringBuilder): Unit = {
    if (count > 0) {
      query += '?'
    }
    var i = 1
    while (i < count) {
      query.append(",?")
      i += 1
    }
  }

  /**
   * Insert a comma separated list of tuples into the query
   * @param numTuples the number of tuples to insert
   * @param tupleSize the size of each tuple
   * @param query the current query in a StringBuilder
   */
  private def insertTuples(numTuples: Int, tupleSize: Int, query: StringBuilder): Unit = {
    if (numTuples > 0) {
      insertTuple(tupleSize, query)
    }
    var i = 1
    while (i < numTuples) {
      query += ','
      insertTuple(tupleSize, query)
      i += 1
    }
  }

  /**
   * Insert a single tuple into the query
   * @param tupleSize the size of the tuple
   * @param query the current query in a StringBuilder
   */
  private def insertTuple(tupleSize: Int, query: StringBuilder): Unit = {
    query += '('
    if (tupleSize > 0) {
      query += '?'
    }
    var i = 1
    while (i < tupleSize) {
      query.append(",?")
      i += 1
    }
    query += ')'
  }
}
