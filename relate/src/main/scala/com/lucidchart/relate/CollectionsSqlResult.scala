package com.lucidchart.relate

import java.sql.ResultSetMetaData
import scala.collection.compat._
import scala.collection.mutable
import scala.language.higherKinds

trait CollectionsSqlResult { self: SqlResult =>

  def asCollection[U, T[_]](parser: SqlRow => U)(implicit factory: Factory[U, T[U]]): T[U] =
    asCollection(parser, Long.MaxValue)
  def asCollection[U: RowParser, T[_]]()(implicit factory: Factory[U, T[U]]): T[U] =
    asCollection(implicitly[RowParser[U]].parse, Long.MaxValue)
  protected def asCollection[U: RowParser, T[_]](maxRows: Long)(implicit factory: Factory[U, T[U]]): T[U] =
    asCollection(implicitly[RowParser[U]].parse, maxRows)
  protected def asCollection[U, T[_]](parser: SqlRow => U, maxRows: Long)(implicit factory: Factory[U, T[U]]): T[U] = {
    val builder = factory.newBuilder

    withResultSet { resultSet =>
      while (resultSet.getRow < maxRows && resultSet.next()) {
        builder += parser(asRow)
      }
    }

    builder.result
  }

  def asPairCollection[U, V, T[_, _]]()(implicit p: RowParser[(U, V)], factory: Factory[(U, V), T[U, V]]): T[U, V] = {
    asPairCollection(p.parse, Long.MaxValue)
  }
  def asPairCollection[U, V, T[_, _]](parser: SqlRow => (U, V))(implicit factory: Factory[(U, V), T[U, V]]): T[U, V] =
    asPairCollection(parser, Long.MaxValue)
  protected def asPairCollection[U, V, T[_, _]](
    maxRows: Long
  )(implicit p: RowParser[(U, V)], factory: Factory[(U, V), T[U, V]]): T[U, V] =
    asPairCollection(p.parse, maxRows)
  protected def asPairCollection[U, V, T[_, _]](parser: SqlRow => (U, V), maxRows: Long)(implicit
    factory: Factory[(U, V), T[U, V]]
  ): T[U, V] = {
    val builder = factory.newBuilder

    withResultSet { resultSet =>
      while (resultSet.getRow < maxRows && resultSet.next()) {
        builder += parser(asRow)
      }
    }

    builder.result
  }

}
