package com.lucidchart.relate

import java.util.Date
import java.time.Instant
import scala.collection.mutable
import scala.language.higherKinds

trait RowParser[A] extends (SqlRow => A) {
  def parse(row: SqlRow): A

  def apply(row: SqlRow) = parse(row)
}

object RowParser {
  def apply[A](f: (SqlRow) => A) = new RowParser[A] {
    def parse(row: SqlRow) = f(row)
  }

  def bigInt(columnLabel: String) = RowParser(_.apply[BigInt](columnLabel))
  def date(columnLabel: String) = RowParser(_.apply[Date](columnLabel))
  def instant(columnLabel: String) = RowParser(_.apply[Instant](columnLabel))
  def int(columnLabel: String) = RowParser(_.apply[Int](columnLabel))
  def long(columnLabel: String) = RowParser(_.apply[Long](columnLabel))
  def string(columnLabel: String) = RowParser(_.apply[String](columnLabel))

  private[relate] val insertInt = (row: SqlRow) => row.strictInt(1)
  private[relate] val insertLong = (row: SqlRow) => row.strictLong(1)
}
