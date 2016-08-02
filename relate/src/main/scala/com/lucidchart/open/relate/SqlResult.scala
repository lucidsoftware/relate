package com.lucidchart.open.relate

import java.io.InputStream
import java.io.Reader
import java.net.URL
import java.nio.ByteBuffer
import java.sql.Blob
import java.sql.Clob
import java.sql.NClob
import java.sql.Ref
import java.sql.ResultSetMetaData
import java.sql.RowId
import java.sql.SQLException
import java.sql.SQLXML
import java.sql.Time
import java.sql.Timestamp
import java.util.Calendar
import java.util.Date
import java.util.UUID
import scala.collection.JavaConversions
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable
import scala.collection.mutable.Builder
import scala.collection.mutable.MutableList
import scala.util.Try

object SqlResult {
  def apply(resultSet: java.sql.ResultSet) = new SqlResult(resultSet)
}

/**
 * The SqlResult class is a wrapper around Java's ResultSet class.
 *
 * It provides methods to allows users to retrieve specific columns by name and datatype,
 * but also provides methods that can, given a [[com.lucidchart.open.relate.RowParser RowParser]],
 * parse the entire result set as a collection of records returned by the parser. These methods are
 * also defined in the Sql trait, and are most conveniently used when chained with parameter
 * insertion. For how to do this, see the [[com.lucidchart.open.relate.Sql Sql]] trait
 * documentation.
 *
 * The extraction methods (int, string, long, etc.) also have "strict" counterparts. The "strict"
 * methods are slightly faster, but do not do type checking or handle null values.
 */
class SqlResult(val resultSet: java.sql.ResultSet) extends ResultSetWrapper {

  def as[A: Parseable](): A = implicitly[Parseable[A]].parse(asRow)

  def asSingle[A: Parseable](): A = asCollection[A, Seq](1).head
  def asSingle[A](parser: SqlRow => A): A = asCollection[A, Seq](parser, 1).head
  def asSingleOption[A: Parseable](): Option[A] = asCollection[A, Seq](1).headOption
  def asSingleOption[A](parser: SqlRow => A): Option[A] = asCollection[A, Seq](parser, 1).headOption
  def asSet[A: Parseable](): Set[A] = asCollection[A, Set](Long.MaxValue)
  def asSet[A](parser: SqlRow => A): Set[A] = asCollection[A, Set](parser, Long.MaxValue)
  def asSeq[A: Parseable](): Seq[A] = asCollection[A, Seq](Long.MaxValue)
  def asSeq[A](parser: SqlRow => A): Seq[A] = asCollection[A, Seq](parser, Long.MaxValue)
  def asIterable[A: Parseable](): Iterable[A] = asCollection[A, Iterable](Long.MaxValue)
  def asIterable[A](parser: SqlRow => A): Iterable[A] = asCollection[A, Iterable](parser, Long.MaxValue)
  def asList[A: Parseable](): List[A] = asCollection[A, List](Long.MaxValue)
  def asList[A](parser: SqlRow => A): List[A] = asCollection[A, List](parser, Long.MaxValue)
  def asMap[U, V]()(implicit p: Parseable[(U, V)]): Map[U, V] = asPairCollection[U, V, Map](Long.MaxValue)
  def asMap[U, V](parser: SqlRow => (U, V)): Map[U, V] = asPairCollection[U, V, Map](parser, Long.MaxValue)
  def asMultiMap[U, V]()(implicit p: Parseable[(U, V)]): Map[U, Set[V]] = asMultiMap(p.parse)
  def asMultiMap[U, V](parser: SqlRow => (U, V)): Map[U, Set[V]] = {
    val mm: mutable.MultiMap[U, V] = new mutable.HashMap[U, mutable.Set[V]] with mutable.MultiMap[U, V]
    withResultSet { resultSet =>
      while (resultSet.next()) {
        val parsed = parser(asRow)
        mm.addBinding(parsed._1, parsed._2)
      }
    }
    mm.toMap.map(x => x._1 -> x._2.toSet)
  }

  def asScalar[A](): A = asScalarOption.get
  def asScalarOption[A](): Option[A] = {
    if (resultSet.next()) {
      Some(resultSet.getObject(1).asInstanceOf[A])
    }
    else {
      None
    }
  }

  def asCollection[U, T[_]](parser: SqlRow => U)(implicit cbf: CanBuildFrom[T[U], U, T[U]]): T[U] = asCollection(parser, Long.MaxValue)
  def asCollection[U: Parseable, T[_]]()(implicit cbf: CanBuildFrom[T[U], U, T[U]]): T[U] = asCollection(implicitly[Parseable[U]].parse, Long.MaxValue)
  protected def asCollection[U: Parseable, T[_]](maxRows: Long)(implicit cbf: CanBuildFrom[T[U], U, T[U]]): T[U] =
    asCollection(implicitly[Parseable[U]].parse, maxRows)
  protected def asCollection[U, T[_]](parser: SqlRow => U, maxRows: Long)(implicit cbf: CanBuildFrom[T[U], U, T[U]]): T[U] = {
    val builder = cbf()

    withResultSet { resultSet =>
      while (resultSet.getRow < maxRows && resultSet.next()) {
        builder += parser(asRow)
      }
    }

    builder.result
  }

  def asPairCollection[U, V, T[_, _]]()(implicit p: Parseable[(U, V)], cbf: CanBuildFrom[T[U, V], (U, V), T[U, V]]): T[U, V] = {
    asPairCollection(p.parse, Long.MaxValue)
  }
  def asPairCollection[U, V, T[_, _]](parser: SqlRow => (U, V))(implicit cbf: CanBuildFrom[T[U, V], (U, V), T[U, V]]): T[U, V] = asPairCollection(parser, Long.MaxValue)
  protected def asPairCollection[U, V, T[_, _]](maxRows: Long)(implicit p: Parseable[(U, V)], cbf: CanBuildFrom[T[U, V], (U, V), T[U, V]]): T[U, V] =
    asPairCollection(p.parse, maxRows)
  protected def asPairCollection[U, V, T[_, _]](parser: SqlRow => (U, V), maxRows: Long)(implicit cbf: CanBuildFrom[T[U, V], (U, V), T[U, V]]): T[U, V] = {
    val builder = cbf()

    withResultSet { resultSet =>
      while (resultSet.getRow < maxRows && resultSet.next()) {
        builder += parser(asRow)
      }
    }

    builder.result
  }

  /**
   * Get the metadata for the java.sql.ResultSet that underlies this SqlResult
   * @return the metadata
   */
  def getMetaData(): ResultSetMetaData = resultSet.getMetaData()

  /**
   * Determine if the result set contains the given column name
   * @param column the column name to check
   * @return whether or not the result set contains that column name
   */
  def hasColumn(column: String): Boolean = {
    try {
      resultSet.findColumn(column)
      true
    }
    catch {
      case e: SQLException => false
    }
  }
}
