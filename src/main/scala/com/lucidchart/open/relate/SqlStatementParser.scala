package com.lucidchart.open.relate

import scala.collection.mutable.MutableList

object SqlStatementParser {

  /**
   * Parse a SQL statement into parameters and replace all parameters with ?
   * @param stmt the statement to process
   * @return a tuple containing the revised SQL statement and the parameter names in order
   */
  def parse(stmt: String): (String, List[String]) = {
    
    val query = new StringBuilder(stmt.length)
    val param = new StringBuilder(100)
    
    var inParam = false
    var params = MutableList[String]()
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
          query.append('?')
          params += param.toString
          inParam = false
          param.clear
        }
        else {
          param.append(c)
        }
      }

      i += 1
    }
    (query.toString, params.toList)
  } 
}
