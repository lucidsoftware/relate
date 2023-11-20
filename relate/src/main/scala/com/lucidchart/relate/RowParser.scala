package com.lucidchart.relate

import java.util.Date
import java.time.Instant
import scala.collection.mutable
import scala.language.higherKinds
import scala.language.implicitConversions

trait RowParser[A] extends (SqlRow => A) {
  def parse(row: SqlRow): A

  def apply(row: SqlRow) = parse(row)
}

object RowParser extends CollectionsParser {
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

  implicit def multiMap[Key: RowParser, Value: RowParser]: RowParser[Map[Key, Set[Value]]] =
    RowParser[Map[Key, Set[Value]]] { result =>
      val mm = new mutable.HashMap[Key, mutable.Builder[Value, Set[Value]]]

      result.withResultSet { resultSet =>
        while (resultSet.next()) {
          val key = implicitly[RowParser[Key]].parse(result)
          val value = implicitly[RowParser[Value]].parse(result)

          val _ = mm.updateWith(key) { vOpt =>
            Some(vOpt.getOrElse(Set.newBuilder).addOne(value))
          }
        }
      }
      mm.view.mapValues(_.result()).toMap
    }

  // Allow implicitly converting
  implicit def funcAsRowParser[A](parser: (SqlRow) => A): RowParser[A] = apply(parser)

}
