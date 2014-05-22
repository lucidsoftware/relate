package com.lucidchart.open.relate

import java.io.InputStream
import java.io.Reader
import java.net.URL
import java.nio.ByteBuffer
import java.sql.Blob
import java.sql.Clob
import java.sql.Connection
import java.sql.NClob
import java.sql.Ref
import java.sql.RowId
import java.sql.SQLXML
import java.sql.Time
import java.sql.Timestamp
import java.util.Calendar
import java.util.Date
import java.util.UUID
import scala.collection.JavaConversions
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable.Builder
import scala.collection.mutable.MutableList

object SqlResult {
  def apply(resultSet: java.sql.ResultSet) = new SqlResult(resultSet)
}

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

  def asSingle[A](parser: RowParser[A])(implicit connection: Connection): A = asCollection[A, Seq](parser, 1).head
  def asSingleOption[A](parser: RowParser[A])(implicit connection: Connection): Option[A] = asCollection[A, Seq](parser, 1).headOption
  def asList[A](parser: RowParser[A])(implicit connection: Connection): List[A] = asCollection[A, List](parser, Long.MaxValue)
  def asMap[U, V](parser: RowParser[(U, V)])(implicit connection: Connection): Map[U, V] = asPairCollection[U, V, Map](parser, Long.MaxValue)

  def asCollection[U, T[_]](parser: RowParser[U])(implicit connection: Connection, cbf: CanBuildFrom[T[U], U, T[U]]): T[U] = asCollection(parser, Long.MaxValue)
  protected def asCollection[U, T[_]](parser: RowParser[U], maxRows: Long)(implicit connection: Connection, cbf: CanBuildFrom[T[U], U, T[U]]): T[U] = {
    val builder = cbf()

    withResultSet { resultSet =>
      while (resultSet.getRow < maxRows && resultSet.next()) {
        builder += parser(this)
      }
    }

    builder.result
  }

  def asPairCollection[U, V, T[_, _]](parser: RowParser[(U, V)])(implicit connection: Connection, cbf: CanBuildFrom[T[U, V], (U, V), T[U, V]]): T[U, V] = asPairCollection(parser, Long.MaxValue)
  def asPairCollection[U, V, T[_, _]](parser: RowParser[(U, V)], maxRows: Long)(implicit connection: Connection, cbf: CanBuildFrom[T[U, V], (U, V), T[U, V]]): T[U, V] = {
    val builder = cbf()

    withResultSet { resultSet =>
      while (resultSet.getRow < maxRows && resultSet.next()) {
        builder += parser(this)
      }
    }

    builder.result
  }

  def getRow(): Int = resultSet.getRow()
  def wasNull(): Boolean = resultSet.wasNull()

  def strictArray(column: String): java.sql.Array = resultSet.getArray(column)
  def strictArrayOption(column: String): Option[java.sql.Array] = Option(resultSet.getArray(column))
  def strictAsciiStream(column: String): InputStream = resultSet.getAsciiStream(column)
  def strictAsciiStreamOption(column: String): Option[InputStream] = Option(resultSet.getAsciiStream(column))
  def strictBigDecimal(column: String): BigDecimal = resultSet.getBigDecimal(column)
  def strictBigDecimalOption(column: String): Option[BigDecimal] = Option(resultSet.getBigDecimal(column))
  def strictBinaryStream(column: String): InputStream = resultSet.getBinaryStream(column)
  def strictBinaryStreamOption(column: String): Option[InputStream] = Option(resultSet.getBinaryStream(column))
  def strictBlob(column: String): Blob = resultSet.getBlob(column)
  def strictBlobOption(column: String): Option[Blob] = Option(resultSet.getBlob(column))
  def strictBoolean(column: String): Boolean = resultSet.getBoolean(column)
  def strictBooleanOption(column: String): Option[Boolean] = Option(resultSet.getBoolean(column))
  def strictByte(column: String): Byte = resultSet.getByte(column)
  def strictByteOption(column: String): Option[Byte] = Option(resultSet.getByte(column))
  def strictBytes(column: String): Array[Byte] = resultSet.getBytes(column)
  def strictBytesOption(column: String): Option[Array[Byte]] = Option(resultSet.getBytes(column))
  def strictCharacterStream(column: String): Reader = resultSet.getCharacterStream(column)
  def strictCharacterStreamOption(column: String): Option[Reader] = Option(resultSet.getCharacterStream(column))
  def strictClob(column: String): Clob = resultSet.getClob(column)
  def strictClobOption(column: String): Option[Clob] = Option(resultSet.getClob(column))
  def strictDate(column: String): Date = resultSet.getDate(column)
  def strictDateOption(column: String): Option[Date] = Option(resultSet.getDate(column))
  def strictDate(column: String, cal: Calendar): Date = resultSet.getDate(column, cal)
  def strictDateOption(column: String, cal: Calendar): Option[Date] = Option(resultSet.getDate(column, cal))
  def strictDouble(column: String): Double = resultSet.getDouble(column)
  def strictDoubleOption(column: String): Option[Double] = Option(resultSet.getDouble(column))
  def strictFloat(column: String): Float = resultSet.getFloat(column)
  def strictFloatOption(column: String): Option[Float] = Option(resultSet.getFloat(column))
  def strictInt(column: String): Int = resultSet.getInt(column)
  def strictIntOption(column: String): Option[Int] = Option(resultSet.getInt(column))
  def strictLong(column: String): Long = resultSet.getLong(column)
  def strictLongOption(column: String): Option[Long] = Option(resultSet.getLong(column))
  def strictNCharacterStream(column: String): Reader = resultSet.getNCharacterStream(column)
  def strictNCharacterStreamOption(column: String): Option[Reader] = Option(resultSet.getNCharacterStream(column))
  def strictNClob(column: String): NClob = resultSet.getNClob(column)
  def strictNClobOption(column: String): Option[NClob] = Option(resultSet.getNClob(column))
  def strictNString(column: String): String = resultSet.getNString(column)
  def strictNStringOption(column: String): Option[String] = Option(resultSet.getNString(column))
  def strictObject(column: String): Object = resultSet.getObject(column)
  def strictObjectOption(column: String): Option[Object] = Option(resultSet.getObject(column))
  def strictObject(column: String, map: Map[String, Class[_]]): Object = resultSet.getObject(column, JavaConversions.mapAsJavaMap(map))
  def strictObjectOption(column: String, map: Map[String, Class[_]]): Option[Object] = Option(resultSet.getObject(column, JavaConversions.mapAsJavaMap(map)))
  def strictRef(column: String): Ref = resultSet.getRef(column)
  def strictRefOption(column: String): Option[Ref] = Option(resultSet.getRef(column))
  def strictRowId(column: String): RowId = resultSet.getRowId(column)
  def strictRowIdOption(column: String): Option[RowId] = Option(resultSet.getRowId(column))
  def strictShort(column: String): Short = resultSet.getShort(column)
  def strictShortOption(column: String): Option[Short] = Option(resultSet.getShort(column))
  def strictSQLXML(column: String): SQLXML = resultSet.getSQLXML(column)
  def strictSQLXMLOption(column: String): Option[SQLXML] = Option(resultSet.getSQLXML(column))
  def strictString(column: String): String = resultSet.getString(column)
  def strictStringOption(column: String): Option[String] = Option(resultSet.getString(column))
  def strictTime(column: String): Time = resultSet.getTime(column)
  def strictTimeOption(column: String): Option[Time] = Option(resultSet.getTime(column))
  def strictTime(column: String, cal: Calendar): Time = resultSet.getTime(column, cal)
  def strictTimeOption(column: String, cal: Calendar): Option[Time] = Option(resultSet.getTime(column, cal))
  def strictTimestamp(column: String): Timestamp = resultSet.getTimestamp(column)
  def strictTimestampOption(column: String): Option[Timestamp] = Option(resultSet.getTimestamp(column))
  def strictTimestamp(column: String, cal: Calendar): Timestamp = resultSet.getTimestamp(column, cal)
  def strictTimestampOption(column: String, cal: Calendar): Option[Timestamp] = Option(resultSet.getTimestamp(column, cal))
  def strictURL(column: String): URL = resultSet.getURL(column)
  def strictURLOption(column: String): Option[URL] = Option(resultSet.getURL(column))

  protected[relate] def extractOption[A](column: String)(f: (Any) => A): Option[A] = {
    Option(resultSet.getObject(column)).map(f)
  }

  def string(column: String): String = stringOption(column).get
  def stringOption(column: String): Option[String] = {
    extractOption(column) {
      case x: String => x
      case x: java.sql.Clob => x.getSubString(1, x.length.asInstanceOf[Int])
    }
  }

  def int(column: String): Int = intOption(column).get
  def intOption(column: String): Option[Int] = Option(resultSet.getInt(column))

  def double(column: String): Double = doubleOption(column).get
  def doubleOption(column: String): Option[Double] = Option(resultSet.getDouble(column))

  def short(column: String): Short = shortOption(column).get
  def shortOption(column: String): Option[Short] = Option(resultSet.getShort(column))

  def byte(column: String): Byte = byteOption(column).get
  def byteOption(column: String): Option[Byte] = Option(resultSet.getByte(column))

  def boolean(column: String): Boolean = booleanOption(column).get
  def booleanOption(column: String): Option[Boolean] = Option(resultSet.getBoolean(column))

  def long(column: String): Long = longOption(column).get
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
  def dateOption(column: String): Option[Date] = Option(resultSet.getTimestamp(column))

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
}

object SqlResultTypes {
  def strictArray(column: String)(implicit sr: SqlResult) = sr.strictArray(column)
  def strictArrayOption(column: String)(implicit sr: SqlResult) = sr.strictArrayOption(column)
  def strictAsciiStream(column: String)(implicit sr: SqlResult) = sr.strictAsciiStream(column)
  def strictAsciiStreamOption(column: String)(implicit sr: SqlResult) = sr.strictAsciiStreamOption(column)
  def strictBigDecimal(column: String)(implicit sr: SqlResult) = sr.strictBigDecimal(column)
  def strictBigDecimalOption(column: String)(implicit sr: SqlResult) = sr.strictBigDecimalOption(column)
  def strictBinaryStream(column: String)(implicit sr: SqlResult) = sr.strictBinaryStream(column)
  def strictBinaryStreamOption(column: String)(implicit sr: SqlResult) = sr.strictBinaryStreamOption(column)
  def strictBlob(column: String)(implicit sr: SqlResult) = sr.strictBlob(column)
  def strictBlobOption(column: String)(implicit sr: SqlResult) = sr.strictBlobOption(column)
  def strictBoolean(column: String)(implicit sr: SqlResult) = sr.strictBoolean(column)
  def strictBooleanOption(column: String)(implicit sr: SqlResult) = sr.strictBooleanOption(column)
  def strictByte(column: String)(implicit sr: SqlResult) = sr.strictByte(column)
  def strictByteOption(column: String)(implicit sr: SqlResult) = sr.strictByteOption(column)
  def strictBytes(column: String)(implicit sr: SqlResult) = sr.strictBytes(column)
  def strictBytesOption(column: String)(implicit sr: SqlResult) = sr.strictBytesOption(column)
  def strictCharacterStream(column: String)(implicit sr: SqlResult) = sr.strictCharacterStream(column)
  def strictCharacterStreamOption(column: String)(implicit sr: SqlResult) = sr.strictCharacterStreamOption(column)
  def strictClob(column: String)(implicit sr: SqlResult) = sr.strictClob(column)
  def strictClobOption(column: String)(implicit sr: SqlResult) = sr.strictClobOption(column)
  def strictDate(column: String)(implicit sr: SqlResult) = sr.strictDate(column)
  def strictDateOption(column: String)(implicit sr: SqlResult) = sr.strictDateOption(column)
  def strictDate(column: String, cal: Calendar)(implicit sr: SqlResult) = sr.strictDate(column, cal)
  def strictDateOption(column: String, cal: Calendar)(implicit sr: SqlResult) = sr.strictDateOption(column, cal)
  def strictDouble(column: String)(implicit sr: SqlResult) = sr.strictDouble(column)
  def strictDoubleOption(column: String)(implicit sr: SqlResult) = sr.strictDoubleOption(column)
  def strictFloat(column: String)(implicit sr: SqlResult) = sr.strictFloat(column)
  def strictFloatOption(column: String)(implicit sr: SqlResult) = sr.strictFloatOption(column)
  def strictInt(column: String)(implicit sr: SqlResult) = sr.strictInt(column)
  def strictIntOption(column: String)(implicit sr: SqlResult) = sr.strictIntOption(column)
  def strictLong(column: String)(implicit sr: SqlResult) = sr.strictLong(column)
  def strictLongOption(column: String)(implicit sr: SqlResult) = sr.strictLongOption(column)
  def strictNCharacterStream(column: String)(implicit sr: SqlResult) = sr.strictNCharacterStream(column)
  def strictNCharacterStreamOption(column: String)(implicit sr: SqlResult) = sr.strictNCharacterStreamOption(column)
  def strictNClob(column: String)(implicit sr: SqlResult) = sr.strictNClob(column)
  def strictNClobOption(column: String)(implicit sr: SqlResult) = sr.strictNClobOption(column)
  def strictNString(column: String)(implicit sr: SqlResult) = sr.strictNString(column)
  def strictNStringOption(column: String)(implicit sr: SqlResult) = sr.strictNStringOption(column)
  def strictObject(column: String)(implicit sr: SqlResult) = sr.strictObject(column)
  def strictObjectOption(column: String)(implicit sr: SqlResult) = sr.strictObjectOption(column)
  def strictObject(column: String, map: Map[String, Class[_]])(implicit sr: SqlResult) = sr.strictObject(column, map)
  def strictObjectOption(column: String, map: Map[String, Class[_]])(implicit sr: SqlResult) = sr.strictObjectOption(column, map)
  def strictRef(column: String)(implicit sr: SqlResult) = sr.strictRef(column)
  def strictRefOption(column: String)(implicit sr: SqlResult) = sr.strictRefOption(column)
  def strictRowId(column: String)(implicit sr: SqlResult) = sr.strictRowId(column)
  def strictRowIdOption(column: String)(implicit sr: SqlResult) = sr.strictRowIdOption(column)
  def strictShort(column: String)(implicit sr: SqlResult) = sr.strictShort(column)
  def strictShortOption(column: String)(implicit sr: SqlResult) = sr.strictShortOption(column)
  def strictSQLXML(column: String)(implicit sr: SqlResult) = sr.strictSQLXML(column)
  def strictSQLXMLOption(column: String)(implicit sr: SqlResult) = sr.strictSQLXMLOption(column)
  def strictString(column: String)(implicit sr: SqlResult) = sr.strictString(column)
  def strictStringOption(column: String)(implicit sr: SqlResult) = sr.strictStringOption(column)
  def strictTime(column: String)(implicit sr: SqlResult) = sr.strictTime(column)
  def strictTimeOption(column: String)(implicit sr: SqlResult) = sr.strictTimeOption(column)
  def strictTime(column: String, cal: Calendar)(implicit sr: SqlResult) = sr.strictTime(column, cal)
  def strictTimeOption(column: String, cal: Calendar)(implicit sr: SqlResult) = sr.strictTimeOption(column, cal)
  def strictTimestamp(column: String)(implicit sr: SqlResult) = sr.strictTimestamp(column)
  def strictTimestampOption(column: String)(implicit sr: SqlResult) = sr.strictTimestampOption(column)
  def strictTimestamp(column: String, cal: Calendar)(implicit sr: SqlResult) = sr.strictTimestamp(column, cal)
  def strictTimestampOption(column: String, cal: Calendar)(implicit sr: SqlResult) = sr.strictTimestampOption(column, cal)
  def strictURL(column: String)(implicit sr: SqlResult) = sr.strictURL(column)
  def strictURLOption(column: String)(implicit sr: SqlResult) = sr.strictURLOption(column)

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
  def boolean(column: String)(implicit sr: SqlResult) = sr.boolean(column)
  def booleanOption(column: String)(implicit sr: SqlResult) = sr.booleanOption(column)
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
}
