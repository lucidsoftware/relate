package com.lucidchart.open.relate

import java.sql.{Date => SqlDate, PreparedStatement, Statement, Timestamp, Types}
import java.math.{BigDecimal => JBigDecimal, BigInteger => JBigInt}
import java.util.{Date, UUID}
import scala.reflect.ClassTag

/** Provide implicit method calls for syntactic sugar */
object Query {
  def commaSeparated(name: String, count: Int)(implicit e: Expandable) = e.commaSeparated(name, count)
  def tupled(name: String, columns: Seq[String], count: Int)(implicit e: Expandable) = e.tupled(name, columns, count)
  def bigDecimal(name: String, value: BigDecimal)(implicit stmt: SqlStatement) = stmt.bigDecimal(name, value)
  def bigDecimal(name: String, value: JBigDecimal)(implicit stmt: SqlStatement) = stmt.bigDecimal(name, value)
  def bigInt(name: String, value: BigInt)(implicit stmt: SqlStatement) = stmt.bigInt(name, value)
  def bigInt(name: String, value: JBigInt)(implicit stmt: SqlStatement) = stmt.bigInt(name, value)
  def bool(name: String, value: Boolean)(implicit stmt: SqlStatement) = stmt.bool(name, value)
  def byte(name: String, value: Byte)(implicit stmt: SqlStatement) = stmt.byte(name, value)
  def byteArray(name: String, value: Array[Byte])(implicit stmt: SqlStatement) = stmt.byteArray(name, value)
  def char(name: String, value: Char)(implicit stmt: SqlStatement) = stmt.char(name, value)
  def date(name: String, value: Date)(implicit stmt: SqlStatement) = stmt.date(name, value)
  def double(name: String, value: Double)(implicit stmt: SqlStatement) = stmt.double(name, value)
  def float(name: String, value: Float)(implicit stmt: SqlStatement) = stmt.float(name, value)
  def int(name: String, value: Int)(implicit stmt: SqlStatement) = stmt.int(name, value)
  def long(name: String, value: Long)(implicit stmt: SqlStatement) = stmt.long(name, value)
  def short(name: String, value: Short)(implicit stmt: SqlStatement) = stmt.short(name, value)
  def string(name: String, value: String)(implicit stmt: SqlStatement) = stmt.string(name, value)
  def timestamp(name: String, value: Timestamp)(implicit stmt: SqlStatement) = stmt.timestamp(name, value)
  def uuid(name: String, value: UUID)(implicit stmt: SqlStatement) = stmt.uuid(name, value)

  // list version
  def bigDecimals(name: String, values: TraversableOnce[BigDecimal])(implicit stmt: SqlStatement) = stmt.bigDecimal(name, values)
  def bigDecimals[X: ClassTag](name: String, values: TraversableOnce[JBigDecimal])(implicit stmt: SqlStatement) = stmt.bigDecimal(name, values)
  def bigInts(name: String, values: TraversableOnce[BigInt])(implicit stmt: SqlStatement) = stmt.bigInt(name, values)
  def bigInts[X: ClassTag](name: String, values: TraversableOnce[JBigInt])(implicit stmt: SqlStatement) = stmt.bigInt(name, values)
  def bools(name: String, values: TraversableOnce[Boolean])(implicit stmt: SqlStatement) = stmt.bool(name, values)
  def bytes(name: String, values: TraversableOnce[Byte])(implicit stmt: SqlStatement) = stmt.byte(name, values)
  def chars(name: String, values: TraversableOnce[Char])(implicit stmt: SqlStatement) = stmt.char(name, values)
  def dates(name: String, values: TraversableOnce[Date])(implicit stmt: SqlStatement) = stmt.date(name, values)
  def doubles(name: String, values: TraversableOnce[Double])(implicit stmt: SqlStatement) = stmt.double(name, values)
  def floats(name: String, values: TraversableOnce[Float])(implicit stmt: SqlStatement) = stmt.float(name, values)
  def ints(name: String, values: TraversableOnce[Int])(implicit stmt: SqlStatement) = stmt.int(name, values)
  def longs(name: String, values: TraversableOnce[Long])(implicit stmt: SqlStatement) = stmt.long(name, values)
  def shorts(name: String, values: TraversableOnce[Short])(implicit stmt: SqlStatement) = stmt.short(name, values)
  def strings(name: String, values: TraversableOnce[String])(implicit stmt: SqlStatement) = stmt.string(name, values)
  def timestamps(name: String, values: TraversableOnce[Timestamp])(implicit stmt: SqlStatement) = stmt.timestamp(name, values)
  def uuids(name: String, values: TraversableOnce[UUID])(implicit stmt: SqlStatement) = stmt.uuid(name, values)

  def bigDecimalOption[A](name: String, value: Option[A])(implicit stmt: SqlStatement, bd: BigDecimalLike[A]) = stmt.bigDecimalOption(name, value)
  def bigIntOption[A](name: String, value: Option[A])(implicit stmt: SqlStatement, bi: BigIntLike[A]) = stmt.bigIntOption(name, value)
  def boolOption[A](name: String, value: Option[Boolean])(implicit stmt: SqlStatement) = stmt.boolOption(name, value)
  def byteOption(name: String, value: Option[Byte])(implicit stmt: SqlStatement) = stmt.byteOption(name, value)
  def byteArrayOption(name: String, value: Option[Array[Byte]])(implicit stmt: SqlStatement) = stmt.byteArrayOption(name, value)
  def charOption(name: String, value: Option[Char])(implicit stmt: SqlStatement) = stmt.charOption(name, value)
  def dateOption(name: String, value: Option[Date])(implicit stmt: SqlStatement) = stmt.dateOption(name, value)
  def doubleOption(name: String, value: Option[Double])(implicit stmt: SqlStatement) = stmt.doubleOption(name, value)
  def floatOption(name: String, value: Option[Float])(implicit stmt: SqlStatement) = stmt.floatOption(name, value)
  def intOption(name: String, value: Option[Int])(implicit stmt: SqlStatement) = stmt.intOption(name, value)
  def longOption(name: String, value: Option[Long])(implicit stmt: SqlStatement) = stmt.longOption(name, value)
  def shortOption(name: String, value: Option[Short])(implicit stmt: SqlStatement) = stmt.shortOption(name, value)
  def stringOption(name: String, value: Option[String])(implicit stmt: SqlStatement) = stmt.stringOption(name, value)
  def timestampOption(name: String, value: Option[Timestamp])(implicit stmt: SqlStatement) = stmt.timestampOption(name, value)
  def uuidOption(name: String, value: Option[UUID])(implicit stmt: SqlStatement) = stmt.uuidOption(name, value)

  trait BigDecimalLike[A] {
    def get(value: A): JBigDecimal
  }

  implicit object BigDecimalWrap extends BigDecimalLike[BigDecimal] {
    def get(value: BigDecimal): JBigDecimal = value.bigDecimal
  }

  implicit object JBigDecimalWrap extends BigDecimalLike[JBigDecimal] {
    def get(value: JBigDecimal): JBigDecimal = value
  }

  trait BigIntLike[A] {
    def get(value: A): JBigInt
  }

  implicit object BigIntWrap extends BigIntLike[BigInt] {
    def get(value: BigInt): JBigInt = value.bigInteger
  }

  implicit object JBigIntWrap extends BigIntLike[JBigInt] {
    def get(value: JBigInt): JBigInt = value
  }
}