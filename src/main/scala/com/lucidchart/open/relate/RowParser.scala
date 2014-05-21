package com.lucidchart.open.relate

trait RowParser[+A] extends (SqlResult => A)
object RowParser {
  def apply[A](f: (SqlResult) => A) = new RowParser[A] {
    def apply(row: SqlResult) = f(row)
  }
}
