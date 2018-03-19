package com.lucidchart.relate

import java.net.URL
import java.nio.ByteBuffer
import java.sql._
import java.time.{LocalDate, LocalTime}
import java.util.UUID

trait Parameterizable[-A] {
  final def contraMap[B](f: B => A) = Parameterizable((statement, i, value: B) => set(statement, i, f(value)), setNull)
  def set(statement: PreparedStatement, i: Int, value: A)
  def setNull(statement: PreparedStatement, i: Int)
  final def setOption(statement: PreparedStatement, i: Int, value: Option[A]) =
    value.fold(setNull(statement, i))(set(statement, i, _))
}

object Parameterizable {
  def apply[A](f: (PreparedStatement, Int, A) => Unit, g: (PreparedStatement, Int) => Unit) = new Parameterizable [A] {
    def set(statement: PreparedStatement, i: Int, value: A) = f(statement, i, value)
    def setNull(statement: PreparedStatement, i: Int) = g(statement, i)
  }

  def from[A, B : Parameterizable](f: A => B) = implicitly[Parameterizable[B]].contraMap(f)

  implicit val array = apply(_.setArray(_, _: Array), _.setNull(_, Types.ARRAY))
  implicit val bigDecimal = apply(_.setBigDecimal(_, _: java.math.BigDecimal), _.setNull(_, Types.DECIMAL))
  implicit val blob = apply(_.setBlob(_, _: Blob), _.setNull(_, Types.BLOB))
  implicit val boolean = apply(_.setBoolean(_, _: Boolean), _.setNull(_, Types.BOOLEAN))
  implicit val byte = apply(_.setByte(_, _: Byte), _.setNull(_, Types.TINYINT))
  implicit val bytes = apply(_.setBytes(_, _: scala.Array[Byte]), _.setNull(_, Types.VARBINARY))
  implicit val clob = apply(_.setClob(_, _: Clob), _.setNull(_, Types.CLOB))
  implicit val date = apply(_.setDate(_, _: Date), _.setNull(_, Types.DATE))
  implicit val double = apply(_.setDouble(_, _: Double), _.setNull(_, Types.DOUBLE))
  implicit val float = apply(_.setFloat(_, _: Float), _.setNull(_, Types.FLOAT))
  implicit val int = apply(_.setInt(_, _: Int), _.setNull(_, Types.INTEGER))
  implicit val long = apply(_.setLong(_, _: Long), _.setNull(_, Types.BIGINT))
  implicit val nClob = apply(_.setNClob(_, _: NClob), _.setNull(_, Types.NCLOB))
  implicit val ref = apply(_.setRef(_, _: Ref), _.setNull(_, Types.REF))
  implicit val rowId = apply(_.setRowId(_, _: RowId), _.setNull(_, Types.ROWID))
  implicit val short = apply(_.setShort(_, _: Short), _.setNull(_, Types.SMALLINT))
  implicit val sqlXml = apply(_.setSQLXML(_, _: SQLXML), _.setNull(_, Types.SQLXML))
  implicit val string = apply(_.setString(_, _: String), _.setNull(_, Types.VARCHAR))
  implicit val time = apply(_.setTime(_, _: Time), _.setNull(_, Types.TIME))
  implicit val timestamp = apply(_.setTimestamp(_, _: Timestamp), _.setNull(_, Types.TIMESTAMP))
  implicit val url = apply(_.setURL(_, _: URL), _.setNull(_, Types.DATALINK))

  implicit val javaDate = from((date: java.util.Date) => new Timestamp(date.getTime))
  implicit val localDate = from(Date.valueOf(_: LocalDate))
  implicit val localTime = from(Time.valueOf(_: LocalTime))
  implicit val instant = from(Timestamp.from)
  implicit val uuid = from { uuid: UUID =>
    val bb = ByteBuffer.wrap(new scala.Array[Byte](16))
    bb.putLong(uuid.getMostSignificantBits)
    bb.putLong(uuid.getLeastSignificantBits)
    bb.array
  }
}
