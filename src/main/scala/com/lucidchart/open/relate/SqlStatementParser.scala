package com.lucidchart.open.relate

import scala.collection.mutable

object SqlStatementParser {

  /**
   * Parse a SQL statement into parameters and replace all parameters with ?
   * @param stmt the statement to process
   * @param listParams a mapping of list parameter names to their sizes
   * @return a tuple containing the revised SQL statement and the parameter names to their index
   */
  def parse(stmt: String, listParams: Map[String, Int] = Map[String, Int]()): (String, Map[String, Int]) = {
    
    val query = new StringBuilder(stmt.length)
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

          if (!listParams.isEmpty && listParams.contains(name)) {
            params ++= insertCommaSeparated(name, listParams(name), query, index)
            index += listParams(name)
          }
          else {
            query.append('?')
            params(name) = index
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
   * @param name the identifier for the parameter
   * @param count the number of parameters in the list
   * @param query the current query in a StringBuilder
   * @param currentIndex the current parameter index the parser is on
   * @return the expanded parameter names and indexes
   */
  def insertCommaSeparated(name: String, count: Int, query: StringBuilder, currentIndex: Int): Map[String, Int] = {
    val params = mutable.Map[String, Int]()

    val paramName = new StringBuilder(name.length + count.toString.length + 1)

    var i = 0
    while (i < count) {
      query += '?'
      query += ','
      paramName ++= name
      paramName += '_'
      paramName ++= i.toString
      params(paramName.toString) = currentIndex + i
      paramName.clear
      i += 1
    }
    query.deleteCharAt(query.length - 1)//drop off the last comma

    params.toMap
  } 
}
