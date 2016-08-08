package com.lucidchart

package object relate {

  implicit class SqlString(string: String) {
    def toSql = InterpolatedQuery.fromParts(Seq(string), Seq())
  }

  implicit class SqlStringContext(stringContext: StringContext) {
    def sql(args: Parameter*) = InterpolatedQuery.fromParts(stringContext.parts, args)
  }

  def tuple(parameters: SingleParameter*) = new TupleParameter(parameters)

}
