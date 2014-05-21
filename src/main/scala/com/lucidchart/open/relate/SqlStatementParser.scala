package com.lucidchart.open.relate

object SqlStatementParser {

  /**
   * Parse a SQL statement into parameters and replace all parameters with ?
   * @param stmt the statement to process
   * @return a tuple containing the revised SQL statement and the parameter names in order
   */
  def parse(stmt: String): (String, List[String]) = {
    
    val query = new StringBuilder(stmt.length)
    val param = new StringBuilder(100)
    val (ignored, params) = stmt.toCharArray.foldLeft((false, Array[String]())) { case ((inParam, params), c) =>  
      if (c == '{') {
        (true, params)
      }
      else if (c == '}' && inParam) {
        query.append('?')
        val ret = (false, params :+ param.toString)
        param.clear
        ret
      }
      else if (inParam) {
        param.append(c)
        (inParam, params)
      } 
      else {
        query.append(c)
        (inParam, params)
      }
    }

    (query.toString, params.toList)
  }
}
