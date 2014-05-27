package com.lucidchart.open.relate

import java.sql.{Date => SqlDate, PreparedStatement, Statement, Timestamp, Types}
import java.math.{BigDecimal => JBigDecimal, BigInteger => JBigInt}
import java.util.{Date, UUID}
import scala.reflect.ClassTag

/** Provide implicit method calls for syntactic sugar */
object Query {
  def commaSeparated(name: String, count: Int)(implicit e: Expandable) = e.commaSeparated(name, count)
  def bigDecimal(name: String, value: BigDecimal)(implicit stmt: SqlStatement) = stmt.bigDecimal(name, value)
  def bigDecimal(name: String, value: JBigDecimal)(implicit stmt: SqlStatement) = stmt.bigDecimal(name, value)
  def bigInt(name: String, value: BigInt)(implicit stmt: SqlStatement) = stmt.bigInt(name, value)
  def bigInt(name: String, value: JBigInt)(implicit stmt: SqlStatement) = stmt.bigInt(name, value)
  def bool(name: String, value: Boolean)(implicit stmt: SqlStatement) = stmt.bool(name, value)
  def byte(name: String, value: Byte)(implicit stmt: SqlStatement) = stmt.byte(name, value)
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
  def bigDecimal(name: String, values: TraversableOnce[BigDecimal])(implicit stmt: SqlStatement) = stmt.bigDecimal(name, values)
  def bigDecimal[X: ClassTag](name: String, values: TraversableOnce[JBigDecimal])(implicit stmt: SqlStatement) = stmt.bigDecimal(name, values)
  def bigInt(name: String, values: TraversableOnce[BigInt])(implicit stmt: SqlStatement) = stmt.bigInt(name, values)
  def bigInt[X: ClassTag](name: String, values: TraversableOnce[JBigInt])(implicit stmt: SqlStatement) = stmt.bigInt(name, values)
  def bool(name: String, values: TraversableOnce[Boolean])(implicit stmt: SqlStatement) = stmt.bool(name, values)
  def byte(name: String, values: TraversableOnce[Byte])(implicit stmt: SqlStatement) = stmt.byte(name, values)
  def char(name: String, values: TraversableOnce[Char])(implicit stmt: SqlStatement) = stmt.char(name, values)
  def date(name: String, values: TraversableOnce[Date])(implicit stmt: SqlStatement) = stmt.date(name, values)
  def double(name: String, values: TraversableOnce[Double])(implicit stmt: SqlStatement) = stmt.double(name, values)
  def float(name: String, values: TraversableOnce[Float])(implicit stmt: SqlStatement) = stmt.float(name, values)
  def int(name: String, values: TraversableOnce[Int])(implicit stmt: SqlStatement) = stmt.int(name, values)
  def long(name: String, values: TraversableOnce[Long])(implicit stmt: SqlStatement) = stmt.long(name, values)
  def short(name: String, values: TraversableOnce[Short])(implicit stmt: SqlStatement) = stmt.short(name, values)
  def string(name: String, values: TraversableOnce[String])(implicit stmt: SqlStatement) = stmt.string(name, values)
  def timestamp(name: String, values: TraversableOnce[Timestamp])(implicit stmt: SqlStatement) = stmt.timestamp(name, values)
  def uuid(name: String, values: TraversableOnce[UUID])(implicit stmt: SqlStatement) = stmt.uuid(name, values)
}