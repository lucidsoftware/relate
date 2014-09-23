package com.lucidchart.open.relate

package object interp {

  implicit class SqlHelper(stringContext: StringContext) {
    def sql(args: Parameter*) = InterpolatedQuery.fromParts(stringContext.parts, args)
  }

  def tuple(parameters: SingleParameter*) = new TupleParameter(parameters)

}
