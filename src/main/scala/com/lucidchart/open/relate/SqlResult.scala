package com.lucidchart.open.relate

import java.sql.Connection
import scala.collection.mutable.MutableList

class SqlResult(resultSet: java.sql.ResultSet) {
  def withResultSet[A](connection: Connection)(f: (java.sql.ResultSet) => A): A = withResultSet(f)(connection)
  def withResultSet[A](f: (java.sql.ResultSet) => A)(implicit connection: Connection) = {
    try {
      f(resultSet)
    }
    finally {
      resultSet.close()
    }
  }

  def asSingle[A](parser: RowParser[A])(implicit connection: Connection): A = asList(parser, 1)(connection).head
  def asSingleOption[A](parser: RowParser[A])(implicit connection: Connection): Option[A] = asList(parser, 1)(connection).headOption
  def asList[A](parser: RowParser[A])(implicit connection: Connection): List[A] = asList(parser, Long.MaxValue)

  def asList[A](parser: RowParser[A], maxRows: Long)(implicit connection: Connection): List[A] = {
    val records = MutableList[A]()
    withResultSet { resultSet =>
      while (resultSet.getRow < maxRows && resultSet.next()) {
        records += parser(Result(resultSet))
      }
    }
    records.toList
  }
}
