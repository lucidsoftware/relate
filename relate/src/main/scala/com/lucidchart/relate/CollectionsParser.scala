package com.lucidchart.relate

import scala.collection.compat._
import scala.language.higherKinds

trait CollectionsParser {
   def limitedCollection[B: RowParser, Col[_]](maxRows: Long)(implicit factory: Factory[B, Col[B]]) =
    RowParser { result =>
      val builder = factory.newBuilder

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

  implicit def collection[B: RowParser, Col[_]](implicit factory: Factory[B, Col[B]]) =
    limitedCollection[B, Col](Long.MaxValue)

  implicit def pairCollection[Key: RowParser, Value: RowParser, PairCol[_, _]]
    (implicit factory: Factory[(Key, Value), PairCol[Key, Value]]) =
    RowParser { result =>

      val builder = factory.newBuilder

      result.withResultSet { resultSet =>
        while (resultSet.getRow < Long.MaxValue && resultSet.next()) {
          builder += implicitly[RowParser[Key]].parse(result) -> implicitly[RowParser[Value]].parse(result)
        }
      }

      builder.result
    }
}