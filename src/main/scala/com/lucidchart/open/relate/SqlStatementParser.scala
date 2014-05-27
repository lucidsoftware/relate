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
  def parse(stmt: String, listParams: Map[String, Int] = Map[String, Int]()): (String, Map[String, Int]) = {
    
    val query = new StringBuilder(stmt.length + 2 * listParams.values.foldLeft(0) (_ + _))
    val param = new StringBuilder(100)
    
    var inParam = false
    val params = mutable.Map[String, Int]()
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

          params(name) = index
          if (!listParams.isEmpty && listParams.contains(name)) {
            val count = listParams(name)
            insertCommaSeparated(count, query)
            index += count
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
  def insertCommaSeparated(count: Int, query: StringBuilder): Unit = {
    if (count > 0) {
      query += '?'
    }
    var i = 1
    while (i < count) {
      query.append(",?")
      i += 1
    }
  } 
}
