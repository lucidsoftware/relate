package com.lucidchart.relate

import java.io.{InputStream, Reader}
import java.net.URL
import java.nio.ByteBuffer
import java.sql.{Blob, Clob, NClob, Ref, RowId, SQLXML, Time, Timestamp}
import java.time.Instant
import java.util.{Calendar, Date, UUID}
import scala.collection.JavaConversions
import scala.language.higherKinds
import scala.util.Try

object SqlRow {
  def apply(rs: java.sql.ResultSet): SqlRow = new SqlRow(rs)
}

class SqlRow(val resultSet: java.sql.ResultSet) extends ResultSetWrapper {
  /**
    * Get the number of the row the SqlResult is currently on
    * @return the current row number
    */
  def getRow(): Int = resultSet.getRow()

  def strictArray(column: String): java.sql.Array = resultSet.getArray(column)
  def strictArrayOption(column: String): Option[java.sql.Array] = getResultSetOption(resultSet.getArray(column))
  def strictAsciiStream(column: String): InputStream = resultSet.getAsciiStream(column)
  def strictAsciiStreamOption(column: String): Option[InputStream] = getResultSetOption(resultSet.getAsciiStream(column))
  def strictBigDecimal(column: String): BigDecimal = resultSet.getBigDecimal(column)
  def strictBigDecimalOption(column: String): Option[BigDecimal] = getResultSetOption(resultSet.getBigDecimal(column))
  def strictBinaryStream(column: String): InputStream = resultSet.getBinaryStream(column)
  def strictBinaryStreamOption(column: String): Option[InputStream] = getResultSetOption(resultSet.getBinaryStream(column))
  def strictBlob(column: String): Blob = resultSet.getBlob(column)
  def strictBlobOption(column: String): Option[Blob] = getResultSetOption(resultSet.getBlob(column))
  def strictBoolean(column: String): Boolean = resultSet.getBoolean(column)
  def strictBooleanOption(column: String): Option[Boolean] = getResultSetOption(resultSet.getBoolean(column))
  def strictByte(column: String): Byte = resultSet.getByte(column)
  def strictByteOption(column: String): Option[Byte] = getResultSetOption(resultSet.getByte(column))
  def strictBytes(column: String): Array[Byte] = resultSet.getBytes(column)
  def strictBytesOption(column: String): Option[Array[Byte]] = getResultSetOption(resultSet.getBytes(column))
  def strictCharacterStream(column: String): Reader = resultSet.getCharacterStream(column)
  def strictCharacterStreamOption(column: String): Option[Reader] = getResultSetOption(resultSet.getCharacterStream(column))
  def strictClob(column: String): Clob = resultSet.getClob(column)
  def strictClobOption(column: String): Option[Clob] = getResultSetOption(resultSet.getClob(column))
  def strictDate(column: String): Date = resultSet.getDate(column)
  def strictDateOption(column: String): Option[Date] = getResultSetOption(resultSet.getDate(column))
  def strictDate(column: String, cal: Calendar): Date = resultSet.getDate(column, cal)
  def strictDateOption(column: String, cal: Calendar): Option[Date] = getResultSetOption(resultSet.getDate(column, cal))
  def strictDouble(column: String): Double = resultSet.getDouble(column)
  def strictDoubleOption(column: String): Option[Double] = getResultSetOption(resultSet.getDouble(column))
  def strictFloat(column: String): Float = resultSet.getFloat(column)
  def strictFloatOption(column: String): Option[Float] = getResultSetOption(resultSet.getFloat(column))
  def strictInt(index: Int): Int = resultSet.getInt(index)
  def strictInt(column: String): Int = resultSet.getInt(column)
  def strictIntOption(column: String): Option[Int] = getResultSetOption(resultSet.getInt(column))
  def strictLong(index: Int): Long = resultSet.getLong(index)
  def strictLong(column: String): Long = resultSet.getLong(column)
  def strictLongOption(column: String): Option[Long] = getResultSetOption(resultSet.getLong(column))
  def strictNCharacterStream(column: String): Reader = resultSet.getNCharacterStream(column)
  def strictNCharacterStreamOption(column: String): Option[Reader] = getResultSetOption(resultSet.getNCharacterStream(column))
  def strictNClob(column: String): NClob = resultSet.getNClob(column)
  def strictNClobOption(column: String): Option[NClob] = getResultSetOption(resultSet.getNClob(column))
  def strictNString(column: String): String = resultSet.getNString(column)
  def strictNStringOption(column: String): Option[String] = getResultSetOption(resultSet.getNString(column))
  def strictObject(column: String): Object = resultSet.getObject(column)
  def strictObjectOption(column: String): Option[Object] = getResultSetOption(resultSet.getObject(column))
  def strictObject(column: String, map: Map[String, Class[_]]): Object = resultSet.getObject(column, JavaConversions.mapAsJavaMap(map))
  def strictObjectOption(column: String, map: Map[String, Class[_]]): Option[Object] = Option(resultSet.getObject(column, JavaConversions.mapAsJavaMap(map)))
  def strictRef(column: String): Ref = resultSet.getRef(column)
  def strictRefOption(column: String): Option[Ref] = getResultSetOption(resultSet.getRef(column))
  def strictRowId(column: String): RowId = resultSet.getRowId(column)
  def strictRowIdOption(column: String): Option[RowId] = getResultSetOption(resultSet.getRowId(column))
  def strictShort(column: String): Short = resultSet.getShort(column)
  def strictShortOption(column: String): Option[Short] = getResultSetOption(resultSet.getShort(column))
  def strictSQLXML(column: String): SQLXML = resultSet.getSQLXML(column)
  def strictSQLXMLOption(column: String): Option[SQLXML] = getResultSetOption(resultSet.getSQLXML(column))
  def strictString(column: String): String = resultSet.getString(column)
  def strictStringOption(column: String): Option[String] = getResultSetOption(resultSet.getString(column))
  def strictTime(column: String): Time = resultSet.getTime(column)
  def strictTimeOption(column: String): Option[Time] = getResultSetOption(resultSet.getTime(column))
  def strictTime(column: String, cal: Calendar): Time = resultSet.getTime(column, cal)
  def strictTimeOption(column: String, cal: Calendar): Option[Time] = getResultSetOption(resultSet.getTime(column, cal))
  def strictTimestamp(column: String): Timestamp = resultSet.getTimestamp(column)
  def strictTimestampOption(column: String): Option[Timestamp] = getResultSetOption(resultSet.getTimestamp(column))
  def strictTimestamp(column: String, cal: Calendar): Timestamp = resultSet.getTimestamp(column, cal)
  def strictTimestampOption(column: String, cal: Calendar): Option[Timestamp] = getResultSetOption(resultSet.getTimestamp(column, cal))
  def strictURL(column: String): URL = resultSet.getURL(column)
  def strictURLOption(column: String): Option[URL] = getResultSetOption(resultSet.getURL(column))

  def apply[A: ColReader](col: String): A =
    implicitly[ColReader[A]].forceRead(col, this)
  def opt[A: ColReader](col: String): Option[A] =
    implicitly[ColReader[A]].read(col, this)

  def string(column: String): String = stringOption(column).get
  def stringOption(column: String): Option[String] = {
    extractOption(column) {
      case x: String => x
      case x: java.sql.Clob => x.getSubString(1, x.length.asInstanceOf[Int])
    }
  }

  def int(column: String): Int = intOption(column).get
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

  def instant(column: String): Instant = instantOption(column).get
  def instantOption(column: String): Option[Instant] = getResultSetOption(resultSet.getTimestamp(column)).map(_.toInstant)

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
    extractOption(column) {
      case u: UUID => u
      case b => {
        val bytes = b match {
          case x: Array[Byte] => x
          case x: Blob => x.getBytes(0, x.length.toInt)
          case x: Clob => x.getSubString(1, x.length.asInstanceOf[Int]).getBytes
          case x: String => x.toCharArray.map(_.toByte)
        }

        require(bytes.length == 16)

        val bb = ByteBuffer.wrap(bytes)
        val high = bb.getLong
        val low = bb.getLong
        new UUID(high, low)
      }
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

  protected def getResultSetOption[A](f: => A): Option[A] = {
    f match {
      case x if (x == null || resultSet.wasNull()) => None
      case x => Some(x)
    }
  }

  protected[relate] def extractOption[A](column: String)(f: (Any) => A): Option[A] = {
    resultSet.getObject(column).asInstanceOf[Any] match {
      case x if (x == null || resultSet.wasNull()) => None
      case x => Some(f(x))
    }
  }
}

/**
  * The SqlResultTypes object provides syntactic sugar for RowParser creation.
  * {{{
  * import com.lucidchart.relate._
  * import com.lucidchart.relate.SqlResultTypes._
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
  def strictArray(column: String)(implicit sr: SqlRow) = sr.strictArray(column)
  def strictArrayOption(column: String)(implicit sr: SqlRow) = sr.strictArrayOption(column)
  def strictAsciiStream(column: String)(implicit sr: SqlRow) = sr.strictAsciiStream(column)
  def strictAsciiStreamOption(column: String)(implicit sr: SqlRow) = sr.strictAsciiStreamOption(column)
  def strictBigDecimal(column: String)(implicit sr: SqlRow) = sr.strictBigDecimal(column)
  def strictBigDecimalOption(column: String)(implicit sr: SqlRow) = sr.strictBigDecimalOption(column)
  def strictBinaryStream(column: String)(implicit sr: SqlRow) = sr.strictBinaryStream(column)
  def strictBinaryStreamOption(column: String)(implicit sr: SqlRow) = sr.strictBinaryStreamOption(column)
  def strictBlob(column: String)(implicit sr: SqlRow) = sr.strictBlob(column)
  def strictBlobOption(column: String)(implicit sr: SqlRow) = sr.strictBlobOption(column)
  def strictBoolean(column: String)(implicit sr: SqlRow) = sr.strictBoolean(column)
  def strictBooleanOption(column: String)(implicit sr: SqlRow) = sr.strictBooleanOption(column)
  def strictByte(column: String)(implicit sr: SqlRow) = sr.strictByte(column)
  def strictByteOption(column: String)(implicit sr: SqlRow) = sr.strictByteOption(column)
  def strictBytes(column: String)(implicit sr: SqlRow) = sr.strictBytes(column)
  def strictBytesOption(column: String)(implicit sr: SqlRow) = sr.strictBytesOption(column)
  def strictCharacterStream(column: String)(implicit sr: SqlRow) = sr.strictCharacterStream(column)
  def strictCharacterStreamOption(column: String)(implicit sr: SqlRow) = sr.strictCharacterStreamOption(column)
  def strictClob(column: String)(implicit sr: SqlRow) = sr.strictClob(column)
  def strictClobOption(column: String)(implicit sr: SqlRow) = sr.strictClobOption(column)
  def strictDate(column: String)(implicit sr: SqlRow) = sr.strictDate(column)
  def strictDateOption(column: String)(implicit sr: SqlRow) = sr.strictDateOption(column)
  def strictDate(column: String, cal: Calendar)(implicit sr: SqlRow) = sr.strictDate(column, cal)
  def strictDateOption(column: String, cal: Calendar)(implicit sr: SqlRow) = sr.strictDateOption(column, cal)
  def strictDouble(column: String)(implicit sr: SqlRow) = sr.strictDouble(column)
  def strictDoubleOption(column: String)(implicit sr: SqlRow) = sr.strictDoubleOption(column)
  def strictFloat(column: String)(implicit sr: SqlRow) = sr.strictFloat(column)
  def strictFloatOption(column: String)(implicit sr: SqlRow) = sr.strictFloatOption(column)
  def strictInt(column: String)(implicit sr: SqlRow) = sr.strictInt(column)
  def strictIntOption(column: String)(implicit sr: SqlRow) = sr.strictIntOption(column)
  def strictLong(column: String)(implicit sr: SqlRow) = sr.strictLong(column)
  def strictLongOption(column: String)(implicit sr: SqlRow) = sr.strictLongOption(column)
  def strictNCharacterStream(column: String)(implicit sr: SqlRow) = sr.strictNCharacterStream(column)
  def strictNCharacterStreamOption(column: String)(implicit sr: SqlRow) = sr.strictNCharacterStreamOption(column)
  def strictNClob(column: String)(implicit sr: SqlRow) = sr.strictNClob(column)
  def strictNClobOption(column: String)(implicit sr: SqlRow) = sr.strictNClobOption(column)
  def strictNString(column: String)(implicit sr: SqlRow) = sr.strictNString(column)
  def strictNStringOption(column: String)(implicit sr: SqlRow) = sr.strictNStringOption(column)
  def strictObject(column: String)(implicit sr: SqlRow) = sr.strictObject(column)
  def strictObjectOption(column: String)(implicit sr: SqlRow) = sr.strictObjectOption(column)
  def strictObject(column: String, map: Map[String, Class[_]])(implicit sr: SqlRow) = sr.strictObject(column, map)
  def strictObjectOption(column: String, map: Map[String, Class[_]])(implicit sr: SqlRow) = sr.strictObjectOption(column, map)
  def strictRef(column: String)(implicit sr: SqlRow) = sr.strictRef(column)
  def strictRefOption(column: String)(implicit sr: SqlRow) = sr.strictRefOption(column)
  def strictRowId(column: String)(implicit sr: SqlRow) = sr.strictRowId(column)
  def strictRowIdOption(column: String)(implicit sr: SqlRow) = sr.strictRowIdOption(column)
  def strictShort(column: String)(implicit sr: SqlRow) = sr.strictShort(column)
  def strictShortOption(column: String)(implicit sr: SqlRow) = sr.strictShortOption(column)
  def strictSQLXML(column: String)(implicit sr: SqlRow) = sr.strictSQLXML(column)
  def strictSQLXMLOption(column: String)(implicit sr: SqlRow) = sr.strictSQLXMLOption(column)
  def strictString(column: String)(implicit sr: SqlRow) = sr.strictString(column)
  def strictStringOption(column: String)(implicit sr: SqlRow) = sr.strictStringOption(column)
  def strictTime(column: String)(implicit sr: SqlRow) = sr.strictTime(column)
  def strictTimeOption(column: String)(implicit sr: SqlRow) = sr.strictTimeOption(column)
  def strictTime(column: String, cal: Calendar)(implicit sr: SqlRow) = sr.strictTime(column, cal)
  def strictTimeOption(column: String, cal: Calendar)(implicit sr: SqlRow) = sr.strictTimeOption(column, cal)
  def strictTimestamp(column: String)(implicit sr: SqlRow) = sr.strictTimestamp(column)
  def strictTimestampOption(column: String)(implicit sr: SqlRow) = sr.strictTimestampOption(column)
  def strictTimestamp(column: String, cal: Calendar)(implicit sr: SqlRow) = sr.strictTimestamp(column, cal)
  def strictTimestampOption(column: String, cal: Calendar)(implicit sr: SqlRow) = sr.strictTimestampOption(column, cal)
  def strictURL(column: String)(implicit sr: SqlRow) = sr.strictURL(column)
  def strictURLOption(column: String)(implicit sr: SqlRow) = sr.strictURLOption(column)

  def string(column: String)(implicit sr: SqlRow) = sr.string(column)
  def stringOption(column: String)(implicit sr: SqlRow) = sr.stringOption(column)
  def int(column: String)(implicit sr: SqlRow) = sr.int(column)
  def intOption(column: String)(implicit sr: SqlRow) = sr.intOption(column)
  def double(column: String)(implicit sr: SqlRow) = sr.double(column)
  def doubleOption(column: String)(implicit sr: SqlRow) = sr.doubleOption(column)
  def short(column: String)(implicit sr: SqlRow) = sr.short(column)
  def shortOption(column: String)(implicit sr: SqlRow) = sr.shortOption(column)
  def byte(column: String)(implicit sr: SqlRow) = sr.byte(column)
  def byteOption(column: String)(implicit sr: SqlRow) = sr.byteOption(column)
  def bool(column: String)(implicit sr: SqlRow) = sr.bool(column)
  def boolOption(column: String)(implicit sr: SqlRow) = sr.boolOption(column)
  def long(column: String)(implicit sr: SqlRow) = sr.long(column)
  def longOption(column: String)(implicit sr: SqlRow) = sr.longOption(column)
  def bigInt(column: String)(implicit sr: SqlRow) = sr.bigInt(column)
  def bigIntOption(column: String)(implicit sr: SqlRow) = sr.bigIntOption(column)
  def bigDecimal(column: String)(implicit sr: SqlRow) = sr.bigDecimal(column)
  def bigDecimalOption(column: String)(implicit sr: SqlRow) = sr.bigDecimalOption(column)
  def javaBigInteger(column: String)(implicit sr: SqlRow) = sr.javaBigInteger(column)
  def javaBigIntegerOption(column: String)(implicit sr: SqlRow) = sr.javaBigIntegerOption(column)
  def javaBigDecimal(column: String)(implicit sr: SqlRow) = sr.javaBigDecimal(column)
  def javaBigDecimalOption(column: String)(implicit sr: SqlRow) = sr.javaBigDecimalOption(column)
  def date(column: String)(implicit sr: SqlRow) = sr.date(column)
  def dateOption(column: String)(implicit sr: SqlRow) = sr.dateOption(column)
  def instant(column: String)(implicit sr: SqlRow) = sr.instant(column)
  def instantOption(column: String)(implicit sr: SqlRow) = sr.instantOption(column)
  def byteArray(column: String)(implicit sr: SqlRow) = sr.byteArray(column)
  def byteArrayOption(column: String)(implicit sr: SqlRow) = sr.byteArrayOption(column)
  def uuid(column: String)(implicit sr: SqlRow) = sr.uuid(column)
  def uuidOption(column: String)(implicit sr: SqlRow) = sr.uuidOption(column)
  def uuidFromString(column: String)(implicit sr: SqlRow) = sr.uuidFromString(column)
  def uuidFromStringOption(column: String)(implicit sr: SqlRow) = sr.uuidFromStringOption(column)
  def enum(column: String, e: Enumeration)(implicit sr: SqlRow) = sr.enum(column, e)
  def enumOption(column: String, e: Enumeration)(implicit sr: SqlRow) = sr.enumOption(column, e)
}
