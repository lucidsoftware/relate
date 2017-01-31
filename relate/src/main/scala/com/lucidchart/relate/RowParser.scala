package com.lucidchart.relate

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable
import scala.language.higherKinds

trait RowParser[A] {
  def parse(row: SqlRow): A
}

object RowParser {
  def apply[A](f: (SqlRow) => A) = new RowParser[A] {
    def parse(row: SqlRow) = f(row)
  }

  def bigInt(columnLabel: String) = (row: SqlRow) => row.bigInt(columnLabel)
  def date(columnLabel: String) = (row: SqlRow) => row.date(columnLabel)
  def int(columnLabel: String) = (row: SqlRow) => row.int(columnLabel)
  def long(columnLabel: String) = (row: SqlRow) => row.long(columnLabel)
  def string(columnLabel: String) = (row: SqlRow) => row.string(columnLabel)

  private[relate] val insertInt = (row: SqlRow) => row.strictInt(1)
  private[relate] val insertLong = (row: SqlRow) => row.strictLong(1)

  def limitedCollection[B: RowParser, Col[_]](maxRows: Long)(implicit cbf: CanBuildFrom[Col[B], B, Col[B]]) =
    RowParser { result =>
      val builder = cbf()

      result.withResultSet { resultSet =>
        while (resultSet.getRow < maxRows && resultSet.next()) {
          builder += implicitly[RowParser[B]].parse(result)
        }
      }

      builder.result
    }

  implicit def option[B: RowParser] = RowParser[Option[B]] { result =>
    limitedCollection[B, List](1).parse(result).headOption
  }

  implicit def collection[B: RowParser, Col[_]](implicit cbf: CanBuildFrom[Col[B], B, Col[B]]) =
    limitedCollection[B, Col](Long.MaxValue)

  implicit def pairCollection[Key: RowParser, Value: RowParser, PairCol[_, _]]
    (implicit cbf: CanBuildFrom[PairCol[Key, Value], (Key, Value), PairCol[Key, Value]]) =
    RowParser { result =>

      val builder = cbf()

      result.withResultSet { resultSet =>
        while (resultSet.getRow < Long.MaxValue && resultSet.next()) {
          builder += implicitly[RowParser[Key]].parse(result) -> implicitly[RowParser[Value]].parse(result)
        }
      }

      builder.result
    }

  implicit def multiMap[Key: RowParser, Value: RowParser] = RowParser[Map[Key, Set[Value]]] { result =>
    val mm: mutable.Map[Key, Set[Value]] = new mutable.HashMap[Key, Set[Value]]

    result.withResultSet { resultSet =>
      while (resultSet.next()) {
        val key = implicitly[RowParser[Key]].parse(result)
        val value = implicitly[RowParser[Value]].parse(result)

        mm.get(key).map { foundValue =>
          mm += (key -> (foundValue + value))
        }.getOrElse {
          mm += (key -> Set(value))
        }
      }
    }
    mm.toMap
  }
}
