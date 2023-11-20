package com.lucidchart.relate

import java.sql.ResultSetMetaData
import scala.collection.mutable
import scala.language.higherKinds

object SqlResult {
  def apply(resultSet: java.sql.ResultSet) = new SqlResult(resultSet)
}

/**
 * The SqlResult class is a wrapper around Java's ResultSet class.
 *
 * It provides methods to allows users to retrieve specific columns by name and datatype, but also provides methods that
 * can, given a [[com.lucidchart.relate.RowParser RowParser]], parse the entire result set as a collection of records
 * returned by the parser. These methods are also defined in the Sql trait, and are most conveniently used when chained
 * with parameter insertion. For how to do this, see the [[com.lucidchart.relate.Sql Sql]] trait documentation.
 *
 * The extraction methods (int, string, long, etc.) also have "strict" counterparts. The "strict" methods are slightly
 * faster, but do not do type checking or handle null values.
 */
class SqlResult(val resultSet: java.sql.ResultSet) extends ResultSetWrapper with CollectionsSqlResult {

  def as[A: RowParser]: A = implicitly[RowParser[A]].parse(asRow)

  def asSingle[A: RowParser]: A = asCollection[A, Seq](1).head
  def asSingleOption[A: RowParser]: Option[A] = asCollection[A, Seq](1).headOption
  def asSet[A: RowParser]: Set[A] = asCollection[A, Set](Long.MaxValue)
  def asSeq[A: RowParser]: Seq[A] = asCollection[A, Seq](Long.MaxValue)
  def asIterable[A: RowParser]: Iterable[A] = asCollection[A, Iterable](Long.MaxValue)
  def asList[A: RowParser]: List[A] = asCollection[A, List](Long.MaxValue)
  def asMap[U, V](implicit p: RowParser[(U, V)]): Map[U, V] = asPairCollection[U, V, Map](Long.MaxValue)
  def asMultiMap[U, V](implicit p: RowParser[(U, V)]): Map[U, Set[V]] = {
    val mm = new mutable.HashMap[U, mutable.Builder[V, Set[V]]]
    withResultSet { resultSet =>
      while (resultSet.next()) {
        val (key, value) = p.parse(asRow)
        val _ = mm.updateWith(key) { vOpt =>
          Some(vOpt.getOrElse(Set.newBuilder).addOne(value))
        }
      }
    }
    mm.view.mapValues(_.result()).toMap
  }

  def asScalar[A]: A = asScalarOption.get
  def asScalarOption[A]: Option[A] = {
    if (resultSet.next()) {
      Some(resultSet.getObject(1).asInstanceOf[A])
    } else {
      None
    }
  }

  /**
   * Get the metadata for the java.sql.ResultSet that underlies this SqlResult
   * @return
   *   the metadata
   */
  def getMetaData(): ResultSetMetaData = resultSet.getMetaData()
}
