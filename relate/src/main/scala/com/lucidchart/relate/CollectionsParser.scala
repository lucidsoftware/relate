package com.lucidchart.relate

import scala.collection.Factory

trait CollectionsParser {
  def limitedCollection[B: RowParser, Col[_]](maxRows: Long)(implicit factory: Factory[B, Col[B]]) =
    RowParser { result =>
      val builder = factory.newBuilder

      result.withResultSet { resultSet =>
        while (resultSet.getRow < maxRows && resultSet.next()) {
          builder += implicitly[RowParser[B]].parse(result)
        }
      }

      builder.result()
    }

  implicit def option[B: RowParser]: RowParser[Option[B]] = RowParser[Option[B]] { result =>
    limitedCollection[B, List](1).parse(result).headOption
  }

  implicit def collection[B: RowParser, Col[_]](implicit factory: Factory[B, Col[B]]): RowParser[Col[B]] =
    limitedCollection[B, Col](Long.MaxValue)

  implicit def pairCollection[Key: RowParser, Value: RowParser, PairCol[_, _]](implicit
    factory: Factory[(Key, Value), PairCol[Key, Value]]
  ): RowParser[PairCol[Key, Value]] =
    RowParser { result =>

      val builder = factory.newBuilder

      result.withResultSet { resultSet =>
        while (resultSet.getRow < Long.MaxValue && resultSet.next()) {
          builder += implicitly[RowParser[Key]].parse(result) -> implicitly[RowParser[Value]].parse(result)
        }
      }

      builder.result()
    }
}
