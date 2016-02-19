package com.lucidchart.open

/**
 * Relate API
 *
 * Use the SQL method to start an SQL query
 *
 * {{{
 * import com.lucidchart.open.relate._
 *
 * SQL("Select 1")
 * }}}
 */
package object relate {
  implicit class SqlString(string: String) {
    def toSql = InterpolatedQuery.fromParts(Seq(string), Seq())
  }

  implicit class SqlStringContext(stringContext: StringContext) {
    def sql(args: Parameter*) = InterpolatedQuery.fromParts(stringContext.parts, args)
  }

  def tuple(parameters: SingleParameter*) = new TupleParameter(parameters)

}