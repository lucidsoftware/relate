package com.lucidchart

package object relate {

  implicit class SqlString(string: String) {
    def unsafeSql = InterpolatedQuery.fromParts(Seq(string), Seq())

    @deprecated("Use .unsafeSql", since = "2.1.0")
    def toSql = unsafeSql
  }

  implicit class SqlStringContext(stringContext: StringContext) {
    def sql(args: Parameter*) = InterpolatedQuery.fromParts(stringContext.parts, args)
  }

  def tuple(parameters: SingleParameter*) = new TupleParameter(parameters)

}
