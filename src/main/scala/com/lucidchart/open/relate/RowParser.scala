package com.lucidchart.open.relate

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable

trait Parseable[A] {
  def parse(row: SqlRow): A
}

object Parseable {
  def apply[A](f: SqlRow => A): Parseable[A] = new Parseable[A] {
    def parse(result: SqlRow): A = f(result)
  }

  def limitedCollection[B: Parseable, Col[_]](maxRows: Long)(implicit cbf: CanBuildFrom[Col[B], B, Col[B]]) =
    Parseable { result =>
      val builder = cbf()

      result.withResultSet { resultSet =>
        while (resultSet.getRow < maxRows && resultSet.next()) {
          builder += implicitly[Parseable[B]].parse(result)
        }
      }

      builder.result
    }

  implicit def option[B: Parseable] = Parseable[Option[B]] { result =>
    limitedCollection[B, List](1).parse(result).headOption
  }

  implicit def collection[B: Parseable, Col[_]](implicit cbf: CanBuildFrom[Col[B], B, Col[B]]) =
    limitedCollection[B, Col](Long.MaxValue)

  implicit def pairCollection[Key: Parseable, Value: Parseable, PairCol[_, _]]
    (implicit cbf: CanBuildFrom[PairCol[Key, Value], (Key, Value), PairCol[Key, Value]]) =
    Parseable { result =>

      val builder = cbf()

      result.withResultSet { resultSet =>
        while (resultSet.getRow < Long.MaxValue && resultSet.next()) {
          builder += implicitly[Parseable[Key]].parse(result) -> implicitly[Parseable[Value]].parse(result)
        }
      }

      builder.result
    }

  implicit def multiMap[Key: Parseable, Value: Parseable] = Parseable[Map[Key, Set[Value]]] { result =>
    val mm: mutable.Map[Key, Set[Value]] = new mutable.HashMap[Key, Set[Value]]

    result.withResultSet { resultSet =>
      while (resultSet.next()) {
        val key = implicitly[Parseable[Key]].parse(result)
        val value = implicitly[Parseable[Value]].parse(result)

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

/**
 * The RowParser companion object allows creation of arbitrary RowParsers with its apply method.
 * {{{
 * import com.lucidchart.open.relate.RowParser
 *
 * val rowParser = (row: SqlResult) =>
 *   (row.long("id"), row.string("name"))
 * }
 * }}}
 */
object RowParser {

  /**
   * Shorthand for creating a RowParser that takes only a BigInt column from the result set
   * @param columnLabel the column name to extract
   * @param the extracted column value
   */
  def bigInt(columnLabel: String) = (row: SqlRow) => row.bigInt(columnLabel)
  /**
   * Shorthand for creating a RowParser that takes only a date column from the result set
   * @param columnLabel the column name to extract
   * @param the extracted column value
   */
  def date(columnLabel: String) = (row: SqlRow) => row.date(columnLabel)
  /**
   * Shorthand for creating a RowParser that takes only an int column from the result set
   * @param columnLabel the column name to extract
   * @param the extracted column value
   */
  def int(columnLabel: String) = (row: SqlRow) => row.int(columnLabel)
  /**
   * Shorthand for creating a RowParser that takes only a long column from the result set
   * @param columnLabel the column name to extract
   * @param the extracted column value
   */
  def long(columnLabel: String) = (row: SqlRow) => row.long(columnLabel)
  /**
   * Shorthand for creating a RowParser that takes only a string column from the result set
   * @param columnLabel the column name to extract
   * @param the extracted column value
   */
  def string(columnLabel: String) = (row: SqlRow) => row.string(columnLabel)

  private[relate] val insertInt = (row: SqlRow) => row.int(1)
  private[relate] val insertLong = (row: SqlRow) => row.long(1)
}
