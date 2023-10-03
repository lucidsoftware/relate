package com.lucidchart.relate

import java.net.URL
import java.nio.ByteBuffer
import java.sql._
import java.time.{LocalDate, LocalTime}
import java.util.UUID

trait Parameterizable[-A] {
  final def contraMap[B](f: B => A) = Parameterizable((statement, i, value: B) => set(statement, i, f(value)), setNull)

  /**
   * Set the parameterized value at index {@code i} in the prepared statement to the {@code value}.
   */
  def set(statement: PreparedStatement, i: Int, value: A): Unit

  /**
   * Set the parameterized value at index {@code i} in the prepared statement to {@code null}.
   */
  def setNull(statement: PreparedStatement, i: Int): Unit
  final def setOption(statement: PreparedStatement, i: Int, value: Option[A]) =
    value.fold(setNull(statement, i))(set(statement, i, _))
}

object Parameterizable {

  /**
   * Create new Parameterizable instance from functions for set and setNull
   *
   * @param f
   *   The function to implement [[Parameterizable#set]] with
   * @param g
   *   The function to implement [[Parameterizable#setNull]] with
   */
  def apply[A](f: (PreparedStatement, Int, A) => Unit, g: (PreparedStatement, Int) => Unit) = new Parameterizable[A] {
    def set(statement: PreparedStatement, i: Int, value: A) = f(statement, i, value)
    def setNull(statement: PreparedStatement, i: Int) = g(statement, i)
  }

  def from[A, B: Parameterizable](f: A => B) = implicitly[Parameterizable[B]].contraMap(f)

  implicit val array: Parameterizable[Array] = apply(_.setArray(_, _: Array), _.setNull(_, Types.ARRAY))
  // ideally, this would be named jBigDecimal, but that wouldn't be  backwards compatibility
  implicit val bigDecimal: Parameterizable[java.math.BigDecimal] =
    apply(_.setBigDecimal(_, _: java.math.BigDecimal), _.setNull(_, Types.DECIMAL))
  implicit val scalaBigDecimal: Parameterizable[scala.math.BigDecimal] = apply(
    (stmt: PreparedStatement, i: Int, v: scala.math.BigDecimal) => stmt.setBigDecimal(i, v.bigDecimal),
    _.setNull(_, Types.DECIMAL)
  )
  implicit val blob: Parameterizable[Blob] = apply(_.setBlob(_, _: Blob), _.setNull(_, Types.BLOB))
  implicit val boolean: Parameterizable[Boolean] = apply(_.setBoolean(_, _: Boolean), _.setNull(_, Types.BOOLEAN))
  implicit val byte: Parameterizable[Byte] = apply(_.setByte(_, _: Byte), _.setNull(_, Types.TINYINT))
  implicit val bytes: Parameterizable[scala.Array[Byte]] =
    apply(_.setBytes(_, _: scala.Array[Byte]), _.setNull(_, Types.VARBINARY))
  implicit val clob: Parameterizable[Clob] = apply(_.setClob(_, _: Clob), _.setNull(_, Types.CLOB))
  implicit val date: Parameterizable[Date] = apply(_.setDate(_, _: Date), _.setNull(_, Types.DATE))
  implicit val double: Parameterizable[Double] = apply(_.setDouble(_, _: Double), _.setNull(_, Types.DOUBLE))
  implicit val float: Parameterizable[Float] = apply(_.setFloat(_, _: Float), _.setNull(_, Types.FLOAT))
  implicit val int: Parameterizable[Int] = apply(_.setInt(_, _: Int), _.setNull(_, Types.INTEGER))
  implicit val long: Parameterizable[Long] = apply(_.setLong(_, _: Long), _.setNull(_, Types.BIGINT))
  implicit val nClob: Parameterizable[NClob] = apply(_.setNClob(_, _: NClob), _.setNull(_, Types.NCLOB))
  implicit val ref: Parameterizable[Ref] = apply(_.setRef(_, _: Ref), _.setNull(_, Types.REF))
  implicit val rowId: Parameterizable[RowId] = apply(_.setRowId(_, _: RowId), _.setNull(_, Types.ROWID))
  implicit val short: Parameterizable[Short] = apply(_.setShort(_, _: Short), _.setNull(_, Types.SMALLINT))
  implicit val sqlXml: Parameterizable[SQLXML] = apply(_.setSQLXML(_, _: SQLXML), _.setNull(_, Types.SQLXML))
  implicit val string: Parameterizable[String] = apply(_.setString(_, _: String), _.setNull(_, Types.VARCHAR))
  implicit val time: Parameterizable[Time] = apply(_.setTime(_, _: Time), _.setNull(_, Types.TIME))
  implicit val timestamp: Parameterizable[Timestamp] =
    apply(_.setTimestamp(_, _: Timestamp), _.setNull(_, Types.TIMESTAMP))
  implicit val url: Parameterizable[URL] = apply(_.setURL(_, _: URL), _.setNull(_, Types.DATALINK))

  // val foo = implicitly[Parameterizable[Timestamp]]
  implicit val javaDate: Parameterizable[java.util.Date] = timestamp.contraMap(d => new Timestamp(d.getTime))
  implicit val localDate: Parameterizable[LocalDate] = date.contraMap(Date.valueOf)
  implicit val localTime: Parameterizable[LocalTime] = time.contraMap(Time.valueOf)
  implicit val instant: Parameterizable[java.time.Instant] = timestamp.contraMap(Timestamp.from)
  implicit val uuid: Parameterizable[UUID] = from { uuid: UUID =>
    val bb = ByteBuffer.wrap(new scala.Array[Byte](16))
    bb.putLong(uuid.getMostSignificantBits)
    bb.putLong(uuid.getLeastSignificantBits)
    bb.array
  }
}
