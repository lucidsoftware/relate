package com.lucidchart.open.relate

import java.nio.ByteBuffer
import java.sql.ResultSet
import java.util.{Date, UUID}

class NullColumnException(col: String) extends Exception(s"Unexpected null value in column: $col")

trait ColReader[A] { self =>
  def read(col: String, row: SqlResult): Option[A]

  def forceRead(col: String, row: SqlResult): A = {
    read(col, row).getOrElse {
      throw new NullColumnException(col)
    }
  }

  def map[B](f: A => B): ColReader[B] = ColReader[B] { (col, rs) =>
    self.read(col, rs).map(f)
  }

  def flatMap[B](f: A => ColReader[B]): ColReader[B] = ColReader[B] { (col, rs) =>
    self.read(col, rs) match {
      case Some(a) => f(a).read(col, rs)
      case None => None
    }
  }

  def filter(f: A => Boolean): ColReader[A] =
    ColReader[A] { (col, rs) => self.read(col, rs).filter(f) }
}

object ColReader {
  def apply[A](f: (String, SqlResult) => Option[A]): ColReader[A] = new ColReader[A] {
    def read(col: String, rs: SqlResult): Option[A] = f(col, rs)
  }

  def option[A](x: A, rs: ResultSet): Option[A] = {
    if (rs.wasNull()) {
      None
    } else {
      Some(x)
    }
  }

  private def optReader[A](f: (String, ResultSet) => A): ColReader[A] = ColReader[A] { (col, row) =>
    option(f(col, row.resultSet), row.resultSet)
  }

  implicit val bigDecimalReader: ColReader[BigDecimal] = ColReader[java.math.BigDecimal] { (col, row) =>
    option(row.resultSet.getBigDecimal(col), row.resultSet)
  }.map(BigDecimal(_))

  implicit val boolReader: ColReader[Boolean] = optReader((col, rs) => rs.getBoolean(col))
  implicit val byteArrayReader: ColReader[Array[Byte]] = optReader((col, rs) => rs.getBytes(col))
  implicit val byteReader: ColReader[Byte] = optReader((col, rs) => rs.getByte(col))
  implicit val dateReader: ColReader[Date] = optReader((col, rs) => rs.getDate(col))
  implicit val doubleReader: ColReader[Double] = optReader((col, rs) => rs.getDouble(col))
  implicit val intReader: ColReader[Int] = optReader((col, rs) => rs.getInt(col))
  implicit val longReader: ColReader[Long] = optReader((col, rs) => rs.getLong(col))
  implicit val shortReader: ColReader[Short] = optReader((col, rs) => rs.getShort(col))
  implicit val stringReader: ColReader[String] = optReader((col, rs) => rs.getString(col))
  implicit val uuidReader: ColReader[UUID] = byteArrayReader.map { bytes =>
    require(bytes.length == 16)
    val bb = ByteBuffer.wrap(bytes)
    val high = bb.getLong
    val low = bb.getLong
    new UUID(high, low)
  }

  def enumReader[A <: Enumeration](e: A): ColReader[e.Value] = {
    intReader.flatMap(id => ColReader[e.Value] { (_, _) =>
      e.values.find(_.id == id)
    })
  }
}
