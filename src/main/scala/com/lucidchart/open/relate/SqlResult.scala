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
class SqlResult(val resultSet: java.sql.ResultSet) {

  protected[relate] def withResultSet[A](f: (java.sql.ResultSet) => A) = {
    try {
      f(resultSet)
    }
    finally {
      resultSet.close()
    }
  }

  def as[A: Parseable](): A = implicitly[Parseable[A]].parse(this)

  def asSingle[A: Parseable](): A = asCollection[A, Seq](1).head
  def asSingle[A](parser: SqlResult => A): A = asCollection[A, Seq](parser, 1).head
  def asSingleOption[A: Parseable](): Option[A] = asCollection[A, Seq](1).headOption
  def asSingleOption[A](parser: SqlResult => A): Option[A] = asCollection[A, Seq](parser, 1).headOption
  def asSet[A: Parseable](): Set[A] = asCollection[A, Set](Long.MaxValue)
  def asSet[A](parser: SqlResult => A): Set[A] = asCollection[A, Set](parser, Long.MaxValue)
  def asSeq[A: Parseable](): Seq[A] = asCollection[A, Seq](Long.MaxValue)
  def asSeq[A](parser: SqlResult => A): Seq[A] = asCollection[A, Seq](parser, Long.MaxValue)
  def asIterable[A: Parseable](): Iterable[A] = asCollection[A, Iterable](Long.MaxValue)
  def asIterable[A](parser: SqlResult => A): Iterable[A] = asCollection[A, Iterable](parser, Long.MaxValue)
  def asList[A: Parseable](): List[A] = asCollection[A, List](Long.MaxValue)
  def asList[A](parser: SqlResult => A): List[A] = asCollection[A, List](parser, Long.MaxValue)
  def asMap[U, V]()(implicit p: Parseable[(U, V)]): Map[U, V] = asPairCollection[U, V, Map](Long.MaxValue)
  def asMap[U, V](parser: SqlResult => (U, V)): Map[U, V] = asPairCollection[U, V, Map](parser, Long.MaxValue)
  def asMultiMap[U, V]()(implicit p: Parseable[(U, V)]): Map[U, Set[V]] = asMultiMap(p.parse)
  def asMultiMap[U, V](parser: SqlResult => (U, V)): Map[U, Set[V]] = {
    val mm: mutable.MultiMap[U, V] = new mutable.HashMap[U, mutable.Set[V]] with mutable.MultiMap[U, V]
    withResultSet { resultSet =>
      while (resultSet.next()) {
        val parsed = parser(this)
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

  def asCollection[U, T[_]](parser: SqlResult => U)(implicit cbf: CanBuildFrom[T[U], U, T[U]]): T[U] = asCollection(parser, Long.MaxValue)
  def asCollection[U: Parseable, T[_]]()(implicit cbf: CanBuildFrom[T[U], U, T[U]]): T[U] = asCollection(implicitly[Parseable[U]].parse, Long.MaxValue)
  protected def asCollection[U: Parseable, T[_]](maxRows: Long)(implicit cbf: CanBuildFrom[T[U], U, T[U]]): T[U] =
    asCollection(implicitly[Parseable[U]].parse, maxRows)
  protected def asCollection[U, T[_]](parser: SqlResult => U, maxRows: Long)(implicit cbf: CanBuildFrom[T[U], U, T[U]]): T[U] = {
    val builder = cbf()

    withResultSet { resultSet =>
      while (resultSet.getRow < maxRows && resultSet.next()) {
        builder += parser(this)
      }
    }

    builder.result
  }

  def asPairCollection[U, V, T[_, _]]()(implicit p: Parseable[(U, V)], cbf: CanBuildFrom[T[U, V], (U, V), T[U, V]]): T[U, V] = {
    asPairCollection(p.parse, Long.MaxValue)
  }
  def asPairCollection[U, V, T[_, _]](parser: SqlResult => (U, V))(implicit cbf: CanBuildFrom[T[U, V], (U, V), T[U, V]]): T[U, V] = asPairCollection(parser, Long.MaxValue)
  protected def asPairCollection[U, V, T[_, _]](maxRows: Long)(implicit p: Parseable[(U, V)], cbf: CanBuildFrom[T[U, V], (U, V), T[U, V]]): T[U, V] =
    asPairCollection(p.parse, maxRows)
  protected def asPairCollection[U, V, T[_, _]](parser: SqlResult => (U, V), maxRows: Long)(implicit cbf: CanBuildFrom[T[U, V], (U, V), T[U, V]]): T[U, V] = {
    val builder = cbf()

    withResultSet { resultSet =>
      while (resultSet.getRow < maxRows && resultSet.next()) {
        builder += parser(this)
      }
    }

    builder.result
  }

  /**
   * Get the number of the row the SqlResult is currently on
   * @return the current row number
   */
  def getRow(): Int = resultSet.getRow()

  /**
   * Get the metadata for the java.sql.ResultSet that underlies this SqlResult
   * @return the metadata
   */
  def getMetaData(): ResultSetMetaData = resultSet.getMetaData()

  /**
   * Determine if the last value extracted from the result set was null
   * @return whether the last value was null
   */
  def wasNull(): Boolean = resultSet.wasNull()
  private[relate] def next(): Boolean = resultSet.next()

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

  protected[relate] def extractOption[A](column: String)(f: (Any) => A): Option[A] = {
    resultSet.getObject(column).asInstanceOf[Any] match {
      case x if (x == null || resultSet.wasNull()) => None
      case x => Some(f(x))
    }
  }

  protected def getResultSetOption[A](f: => A): Option[A] = {
    f match {
      case x if (x == null || resultSet.wasNull()) => None
      case x => Some(x)
    }
  }

  def string(column: String): String = stringOption(column).get
  def stringOption(column: String): Option[String] = {
    extractOption(column) {
      case x: String => x
      case x: java.sql.Clob => x.getSubString(1, x.length.asInstanceOf[Int])
    }
  }

  def int(column: String): Int = intOption(column).get
  private[relate] def int(index: Int): Int = resultSet.getInt(index)
  def intOption(column: String): Option[Int] = getResultSetOption(resultSet.getInt(column))

  def double(column: String): Double = doubleOption(column).get
  def doubleOption(column: String): Option[Double] = getResultSetOption(resultSet.getDouble(column))

  def short(column: String): Short = shortOption(column).get
  def shortOption(column: String): Option[Short] = getResultSetOption(resultSet.getShort(column))

  def byte(column: String): Byte = byteOption(column).get
  def byteOption(column: String): Option[Byte] = getResultSetOption(resultSet.getByte(column))

  def bool(column: String): Boolean = boolOption(column).get
  def boolOption(column: String): Option[Boolean] = getResultSetOption(resultSet.getBoolean(column))

  def long(column: String): Long = longOption(column).get
  private[relate] def long(index: Int): Long = resultSet.getLong(index)
  def longOption(column: String): Option[Long] = {
    extractOption(column) {
      case x: Long => x
      case x: Int => x.toLong
    }
  }

  def bigInt(column: String): BigInt = bigIntOption(column).get
  def bigIntOption(column: String): Option[BigInt] = {
    extractOption(column) {
      case x: Int => BigInt(x)
      case x: Long => BigInt(x)
      case x: String => BigInt(x)
      case x: java.math.BigInteger => BigInt(x.toString)
    }
  }

  def bigDecimal(column: String): BigDecimal = bigDecimalOption(column).get
  def bigDecimalOption(column: String): Option[BigDecimal] = {
    extractOption(column) {
      case x: Int => BigDecimal(x)
      case x: Long => BigDecimal(x)
      case x: String => BigDecimal(x)
      case x: java.math.BigDecimal => BigDecimal(x.toString)
    }
  }

  def javaBigInteger(column: String): java.math.BigInteger = javaBigIntegerOption(column).get
  def javaBigIntegerOption(column: String): Option[java.math.BigInteger] = {
    extractOption(column) {
      case x: java.math.BigInteger => x
      case x: Int => java.math.BigInteger.valueOf(x)
      case x: Long => java.math.BigInteger.valueOf(x)
    }
  }

  def javaBigDecimal(column: String): java.math.BigDecimal = javaBigDecimalOption(column).get
  def javaBigDecimalOption(column: String): Option[java.math.BigDecimal] = {
    extractOption(column) {
      case x: java.math.BigDecimal => x
      case x: Double => new java.math.BigDecimal(x)
    }
  }

  def date(column: String): Date = dateOption(column).get
  def dateOption(column: String): Option[Date] = getResultSetOption(resultSet.getTimestamp(column))

  def byteArray(column: String): Array[Byte] = byteArrayOption(column).get
  def byteArrayOption(column: String): Option[Array[Byte]] = {
    extractOption(column) {
      case x: Array[Byte] => x
      case x: Blob => x.getBytes(0, x.length.toInt)
      case x: Clob => x.getSubString(1, x.length.asInstanceOf[Int]).getBytes
      case x: String => x.toCharArray.map(_.toByte)
    }
  }

  def uuid(column: String): UUID = uuidOption(column).get
  def uuidOption(column: String): Option[UUID] = {
    byteArrayOption(column).map { bytes =>
      require(bytes.length == 16)

      val bb = ByteBuffer.wrap(bytes)
      val high = bb.getLong
      val low = bb.getLong
      new UUID(high, low)
    }
  }

  private val hexReplaceRegex = """[^a-fA-F0-9]""".r
  def uuidFromString(column: String): UUID = uuidFromStringOption(column).get
  def uuidFromStringOption(column: String): Option[UUID] = {
    stringOption(column).map { string =>
      val hex = hexReplaceRegex.replaceAllIn(string, "")
      require(hex.length == 32)

      UUID.fromString(
        hex.substring( 0,  8) + "-" +
        hex.substring( 8, 12) + "-" +
        hex.substring(12, 16) + "-" +
        hex.substring(16, 20) + "-" +
        hex.substring(20, 32)
      )
    }
  }

  def enum(column: String, e: Enumeration) = enumOption(column, e).get
  def enumOption(column: String, e: Enumeration): Option[e.Value] = for {
    id <- intOption(column)
    value <- Try(e(id)).toOption
  } yield(value)
}

/**
 * The SqlResultTypes object provides syntactic sugar for RowParser creation.
 * {{{
 * import com.lucidchart.open.relate._
 * import com.lucidchart.open.relate.SqlResultTypes._
 *
 * val rowParser = RowParser { implicit row =>
 *   (long("id"), string("name"))
 * }
 * }}}
 *
 * In this example, declaring "row" as implicit precludes the need to explicitly use the long and
 * string methods on "row".
 */
object SqlResultTypes {
  def string(column: String)(implicit sr: SqlResult) = sr.string(column)
  def stringOption(column: String)(implicit sr: SqlResult) = sr.stringOption(column)
  def int(column: String)(implicit sr: SqlResult) = sr.int(column)
  def intOption(column: String)(implicit sr: SqlResult) = sr.intOption(column)
  def double(column: String)(implicit sr: SqlResult) = sr.double(column)
  def doubleOption(column: String)(implicit sr: SqlResult) = sr.doubleOption(column)
  def short(column: String)(implicit sr: SqlResult) = sr.short(column)
  def shortOption(column: String)(implicit sr: SqlResult) = sr.shortOption(column)
  def byte(column: String)(implicit sr: SqlResult) = sr.byte(column)
  def byteOption(column: String)(implicit sr: SqlResult) = sr.byteOption(column)
  def bool(column: String)(implicit sr: SqlResult) = sr.bool(column)
  def boolOption(column: String)(implicit sr: SqlResult) = sr.boolOption(column)
  def long(column: String)(implicit sr: SqlResult) = sr.long(column)
  def longOption(column: String)(implicit sr: SqlResult) = sr.longOption(column)
  def bigInt(column: String)(implicit sr: SqlResult) = sr.bigInt(column)
  def bigIntOption(column: String)(implicit sr: SqlResult) = sr.bigIntOption(column)
  def bigDecimal(column: String)(implicit sr: SqlResult) = sr.bigDecimal(column)
  def bigDecimalOption(column: String)(implicit sr: SqlResult) = sr.bigDecimalOption(column)
  def javaBigInteger(column: String)(implicit sr: SqlResult) = sr.javaBigInteger(column)
  def javaBigIntegerOption(column: String)(implicit sr: SqlResult) = sr.javaBigIntegerOption(column)
  def javaBigDecimal(column: String)(implicit sr: SqlResult) = sr.javaBigDecimal(column)
  def javaBigDecimalOption(column: String)(implicit sr: SqlResult) = sr.javaBigDecimalOption(column)
  def date(column: String)(implicit sr: SqlResult) = sr.date(column)
  def dateOption(column: String)(implicit sr: SqlResult) = sr.dateOption(column)
  def byteArray(column: String)(implicit sr: SqlResult) = sr.byteArray(column)
  def byteArrayOption(column: String)(implicit sr: SqlResult) = sr.byteArrayOption(column)
  def uuid(column: String)(implicit sr: SqlResult) = sr.uuid(column)
  def uuidOption(column: String)(implicit sr: SqlResult) = sr.uuidOption(column)
  def uuidFromString(column: String)(implicit sr: SqlResult) = sr.uuidFromString(column)
  def uuidFromStringOption(column: String)(implicit sr: SqlResult) = sr.uuidFromStringOption(column)
  def enum(column: String, e: Enumeration)(implicit sr: SqlResult) = sr.enum(column, e)
  def enumOption(column: String, e: Enumeration)(implicit sr: SqlResult) = sr.enumOption(column, e)
}
