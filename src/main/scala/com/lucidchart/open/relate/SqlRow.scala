package com.lucidchart.open.relate

import java.nio.ByteBuffer
import java.sql.{ Blob, Clob, SQLException }
import java.util.{ Date, UUID }
import scala.util.Try


class SqlRow(val resultSet: java.sql.ResultSet) extends WrappedResultSet {
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