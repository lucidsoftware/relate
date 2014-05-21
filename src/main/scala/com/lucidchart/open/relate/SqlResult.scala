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

  def asSingle[A](parser: RowParser[A])(implicit connection: Connection): A = asList(parser, 1)(connection).head
  def asSingleOption[A](parser: RowParser[A])(implicit connection: Connection): Option[A] = asList(parser, 1)(connection).headOption
  def asList[A](parser: RowParser[A])(implicit connection: Connection): List[A] = asList(parser, Long.MaxValue)

  def asList[A](parser: RowParser[A], maxRows: Long)(implicit connection: Connection): List[A] = {
    val records = MutableList[A]()
    withResultSet { resultSet =>
      while (resultSet.getRow < maxRows && resultSet.next()) {
        records += parser(this)
      }
    }
    records.toList
  }

  def getRow(): Int = resultSet.getRow()
  def wasNull(): Boolean = resultSet.wasNull()

  def strictArray(columnLabel: String): java.sql.Array = resultSet.getArray(columnLabel)
  def strictArrayOption(columnLabel: String): Option[java.sql.Array] = Option(resultSet.getArray(columnLabel))
  def strictAsciiStream(columnLabel: String): InputStream = resultSet.getAsciiStream(columnLabel)
  def strictAsciiStreamOption(columnLabel: String): Option[InputStream] = Option(resultSet.getAsciiStream(columnLabel))
  def strictBigDecimal(columnLabel: String): BigDecimal = resultSet.getBigDecimal(columnLabel)
  def strictBigDecimalOption(columnLabel: String): Option[BigDecimal] = Option(resultSet.getBigDecimal(columnLabel))
  def strictBinaryStream(columnLabel: String): InputStream = resultSet.getBinaryStream(columnLabel)
  def strictBinaryStreamOption(columnLabel: String): Option[InputStream] = Option(resultSet.getBinaryStream(columnLabel))
  def strictBlob(columnLabel: String): Blob = resultSet.getBlob(columnLabel)
  def strictBlobOption(columnLabel: String): Option[Blob] = Option(resultSet.getBlob(columnLabel))
  def strictBoolean(columnLabel: String): Boolean = resultSet.getBoolean(columnLabel)
  def strictBooleanOption(columnLabel: String): Option[Boolean] = Option(resultSet.getBoolean(columnLabel))
  def strictByte(columnLabel: String): Byte = resultSet.getByte(columnLabel)
  def strictByteOption(columnLabel: String): Option[Byte] = Option(resultSet.getByte(columnLabel))
  def strictBytes(columnLabel: String): Array[Byte] = resultSet.getBytes(columnLabel)
  def strictBytesOption(columnLabel: String): Option[Array[Byte]] = Option(resultSet.getBytes(columnLabel))
  def strictCharacterStream(columnLabel: String): Reader = resultSet.getCharacterStream(columnLabel)
  def strictCharacterStreamOption(columnLabel: String): Option[Reader] = Option(resultSet.getCharacterStream(columnLabel))
  def strictClob(columnLabel: String): Clob = resultSet.getClob(columnLabel)
  def strictClobOption(columnLabel: String): Option[Clob] = Option(resultSet.getClob(columnLabel))
  def strictDate(columnLabel: String): Date = resultSet.getDate(columnLabel)
  def strictDateOption(columnLabel: String): Option[Date] = Option(resultSet.getDate(columnLabel))
  def strictDate(columnLabel: String, cal: Calendar): Date = resultSet.getDate(columnLabel, cal)
  def strictDateOption(columnLabel: String, cal: Calendar): Option[Date] = Option(resultSet.getDate(columnLabel, cal))
  def strictDouble(columnLabel: String): Double = resultSet.getDouble(columnLabel)
  def strictDoubleOption(columnLabel: String): Option[Double] = Option(resultSet.getDouble(columnLabel))
  def strictFloat(columnLabel: String): Float = resultSet.getFloat(columnLabel)
  def strictFloatOption(columnLabel: String): Option[Float] = Option(resultSet.getFloat(columnLabel))
  def strictInt(columnLabel: String): Int = resultSet.getInt(columnLabel)
  def strictIntOption(columnLabel: String): Option[Int] = Option(resultSet.getInt(columnLabel))
  def strictLong(columnLabel: String): Long = resultSet.getLong(columnLabel)
  def strictLongOption(columnLabel: String): Option[Long] = Option(resultSet.getLong(columnLabel))
  def strictNCharacterStream(columnLabel: String): Reader = resultSet.getNCharacterStream(columnLabel)
  def strictNCharacterStreamOption(columnLabel: String): Option[Reader] = Option(resultSet.getNCharacterStream(columnLabel))
  def strictNClob(columnLabel: String): NClob = resultSet.getNClob(columnLabel)
  def strictNClobOption(columnLabel: String): Option[NClob] = Option(resultSet.getNClob(columnLabel))
  def strictNString(columnLabel: String): String = resultSet.getNString(columnLabel)
  def strictNStringOption(columnLabel: String): Option[String] = Option(resultSet.getNString(columnLabel))
  def strictObject(columnLabel: String): Object = resultSet.getObject(columnLabel)
  def strictObjectOption(columnLabel: String): Option[Object] = Option(resultSet.getObject(columnLabel))
  def strictObject(columnLabel: String, map: Map[String, Class[_]]): Object = resultSet.getObject(columnLabel, JavaConversions.mapAsJavaMap(map))
  def strictObjectOption(columnLabel: String, map: Map[String, Class[_]]): Option[Object] = Option(resultSet.getObject(columnLabel, JavaConversions.mapAsJavaMap(map)))
  def strictRef(columnLabel: String): Ref = resultSet.getRef(columnLabel)
  def strictRefOption(columnLabel: String): Option[Ref] = Option(resultSet.getRef(columnLabel))
  def strictRowId(columnLabel: String): RowId = resultSet.getRowId(columnLabel)
  def strictRowIdOption(columnLabel: String): Option[RowId] = Option(resultSet.getRowId(columnLabel))
  def strictShort(columnLabel: String): Short = resultSet.getShort(columnLabel)
  def strictShortOption(columnLabel: String): Option[Short] = Option(resultSet.getShort(columnLabel))
  def strictSQLXML(columnLabel: String): SQLXML = resultSet.getSQLXML(columnLabel)
  def strictSQLXMLOption(columnLabel: String): Option[SQLXML] = Option(resultSet.getSQLXML(columnLabel))
  def strictString(columnLabel: String): String = resultSet.getString(columnLabel)
  def strictStringOption(columnLabel: String): Option[String] = Option(resultSet.getString(columnLabel))
  def strictTime(columnLabel: String): Time = resultSet.getTime(columnLabel)
  def strictTimeOption(columnLabel: String): Option[Time] = Option(resultSet.getTime(columnLabel))
  def strictTime(columnLabel: String, cal: Calendar): Time = resultSet.getTime(columnLabel, cal)
  def strictTimeOption(columnLabel: String, cal: Calendar): Option[Time] = Option(resultSet.getTime(columnLabel, cal))
  def strictTimestamp(columnLabel: String): Timestamp = resultSet.getTimestamp(columnLabel)
  def strictTimestampOption(columnLabel: String): Option[Timestamp] = Option(resultSet.getTimestamp(columnLabel))
  def strictTimestamp(columnLabel: String, cal: Calendar): Timestamp = resultSet.getTimestamp(columnLabel, cal)
  def strictTimestampOption(columnLabel: String, cal: Calendar): Option[Timestamp] = Option(resultSet.getTimestamp(columnLabel, cal))
  def strictURL(columnLabel: String): URL = resultSet.getURL(columnLabel)
  def strictURLOption(columnLabel: String): Option[URL] = Option(resultSet.getURL(columnLabel))

  protected[relate] def extractOption[A](columnLabel: String)(f: (Any) => A): Option[A] = {
    Option(resultSet.getObject(columnLabel)).map(f)
  }

  def safeString(columnLabel: String): String = safeStringOption(columnLabel).get
  def safeStringOption(columnLabel: String): Option[String] = {
    extractOption(columnLabel) {
      case x: String => x
      case x: java.sql.Clob => x.getSubString(1, x.length.asInstanceOf[Int])
    }
  }

  def safeInt(columnLabel: String): Int = safeIntOption(columnLabel).get
  def safeIntOption(columnLabel: String): Option[Int] = Option(resultSet.getInt(columnLabel))

  def safeDouble(columnLabel: String): Double = safeDoubleOption(columnLabel).get
  def safeDoubleOption(columnLabel: String): Option[Double] = Option(resultSet.getDouble(columnLabel))

  def safeShort(columnLabel: String): Short = safeShortOption(columnLabel).get
  def safeShortOption(columnLabel: String): Option[Short] = Option(resultSet.getShort(columnLabel))

  def safeByte(columnLabel: String): Byte = safeByteOption(columnLabel).get
  def safeByteOption(columnLabel: String): Option[Byte] = Option(resultSet.getByte(columnLabel))

  def safeBoolean(columnLabel: String): Boolean = safeBooleanOption(columnLabel).get
  def safeBooleanOption(columnLabel: String): Option[Boolean] = Option(resultSet.getBoolean(columnLabel))

  def safeLong(columnLabel: String): Long = safeLongOption(columnLabel).get
  def safeLongOption(columnLabel: String): Option[Long] = {
    extractOption(columnLabel) {
      case x: Long => x
      case x: Int => x.toLong
    }
  }

  def safeBigInt(columnLabel: String): BigInt = safeBigIntOption(columnLabel).get
  def safeBigIntOption(columnLabel: String): Option[BigInt] = {
    extractOption(columnLabel) {
      case x: Int => BigInt(x)
      case x: Long => BigInt(x)
      case x: String => BigInt(x)
      case x: java.math.BigInteger => BigInt(x.toString)
    }
  }

  def safeBigDecimal(columnLabel: String): BigDecimal = safeBigDecimalOption(columnLabel).get
  def safeBigDecimalOption(columnLabel: String): Option[BigDecimal] = {
    extractOption(columnLabel) {
      case x: Int => BigDecimal(x)
      case x: Long => BigDecimal(x)
      case x: String => BigDecimal(x)
      case x: java.math.BigDecimal => BigDecimal(x.toString)
    }
  }

  def safeJavaBigInteger(columnLabel: String): java.math.BigInteger = safeJavaBigIntegerOption(columnLabel).get
  def safeJavaBigIntegerOption(columnLabel: String): Option[java.math.BigInteger] = {
    extractOption(columnLabel) {
      case x: java.math.BigInteger => x
      case x: Int => java.math.BigInteger.valueOf(x)
      case x: Long => java.math.BigInteger.valueOf(x)
    }
  }

  def safeJavaBigDecimal(columnLabel: String): java.math.BigDecimal = safeJavaBigDecimalOption(columnLabel).get
  def safeJavaBigDecimalOption(columnLabel: String): Option[java.math.BigDecimal] = {
    extractOption(columnLabel) {
      case x: java.math.BigDecimal => x
      case x: Double => new java.math.BigDecimal(x)
    }
  }

  def safeDate(columnLabel: String): Date = safeDateOption(columnLabel).get
  def safeDateOption(columnLabel: String): Option[Date] = Option(resultSet.getTimestamp(columnLabel))

  def safeByteArray(columnLabel: String): Array[Byte] = safeByteArrayOption(columnLabel).get
  def safeByteArrayOption(columnLabel: String): Option[Array[Byte]] = {
    extractOption(columnLabel) {
      case x: Array[Byte] => x
      case x: Blob => x.getBytes(0, x.length.toInt)
      case x: Clob => x.getSubString(1, x.length.asInstanceOf[Int]).getBytes
      case x: String => x.toCharArray.map(_.toByte)
    }
  }

  def safeUUIDFromByteArray(columnLabel: String): UUID = safeUUIDFromByteArrayOption(columnLabel).get
  def safeUUIDFromByteArrayOption(columnLabel: String): Option[UUID] = {
    safeByteArrayOption(columnLabel).map { bytes =>
      require(bytes.length == 16)

      val bb = ByteBuffer.wrap(bytes)
      val high = bb.getLong
      val low = bb.getLong
      new UUID(high, low)
    }
  }

  private val hexReplaceRegex = """[^a-fA-F0-9]""".r
  def safeUUIDFromString(columnLabel: String): UUID = safeUUIDFromStringOption(columnLabel).get
  def safeUUIDFromStringOption(columnLabel: String): Option[UUID] = {
    safeStringOption(columnLabel).map { string =>
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
