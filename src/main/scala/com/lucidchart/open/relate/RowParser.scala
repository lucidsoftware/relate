package com.lucidchart.open.relate

trait RowParser[+A] extends (SqlResult => A)
object RowParser {
  def apply[A](f: (SqlResult) => A) = new RowParser[A] {
    def apply(row: SqlResult) = f(row)
  }

  def bigInt(columnLabel: String) = RowParser { row => row.bigInt(columnLabel) }
  def date(columnLabel: String) = RowParser { row => row.date(columnLabel) }
  def int(columnLabel: String) = RowParser { row => row.int(columnLabel) }
  def long(columnLabel: String) = RowParser { row => row.long(columnLabel) }
  def string(columnLabel: String) = RowParser { row => row.string(columnLabel) }

  val insertInt = RowParser { row => row.strictInt(1) }
  val insertLong = RowParser { row => row.strictLong(1) }
}
