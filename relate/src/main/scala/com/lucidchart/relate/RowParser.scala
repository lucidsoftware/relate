package com.lucidchart.relate

import java.util.Date
import java.time.Instant
import scala.collection.mutable
import scala.language.higherKinds
import scala.language.implicitConversions

trait RowParser[A] extends (SqlRow => A) { self =>
  def parse(row: SqlRow): A

  def apply(row: SqlRow) = parse(row)

  override def andThen[B](g: A => B): RowParser[B] = new RowParser[B] {
    def parse(row: SqlRow): B = g(self.parse(row))
  }

  def map[B](f: A => B): RowParser[B] = andThen(f)
}

object RowParser {
  def apply[A](f: (SqlRow) => A) = new RowParser[A] {
    def parse(row: SqlRow) = f(row)
  }

  def bigInt(columnLabel: String): RowParser[BigInt] = r => r.apply[BigInt](columnLabel)
  def date(columnLabel: String): RowParser[Date] = r => r.apply[Date](columnLabel)
  def instant(columnLabel: String): RowParser[Instant] = r => r.apply[Instant](columnLabel)
  def int(columnLabel: String): RowParser[Int] = r => r.apply[Int](columnLabel)
  def long(columnLabel: String): RowParser[Long] = r => r.apply[Long](columnLabel)
  def string(columnLabel: String): RowParser[String] = r => r.apply[String](columnLabel)

  private[relate] val insertInt: RowParser[Int] = row => row.strictInt(1)
  private[relate] val insertLong: RowParser[Long] = row => row.strictLong(1)
}
