package com.lucidchart.relate

import java.time.{Instant, LocalDate}
import java.util.{Date, UUID}

class NullColumnException(col: String) extends Exception(s"Unexpected null value in column: $col")

trait ColReader[A] { self =>
  def read(col: String, row: SqlRow): Option[A]

  def forceRead(col: String, row: SqlRow): A = {
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
  def apply[A](f: (String, SqlRow) => Option[A]): ColReader[A] = new ColReader[A] {
    def read(col: String, rs: SqlRow): Option[A] = f(col, rs)
  }

  implicit val jbigDecimalReader: ColReader[java.math.BigDecimal] = ColReader { (col, row) => row.javaBigDecimalOption(col)}
  implicit val bigDecimalReader: ColReader[BigDecimal] = ColReader { (col, row) => row.bigDecimalOption(col)}
  implicit val jBigIntReader: ColReader[java.math.BigInteger] = ColReader { (col, row) => row.javaBigIntegerOption(col)}
  implicit val bigIntReader: ColReader[BigInt] = ColReader { (col, row) => row.bigIntOption(col)}
  implicit val boolReader: ColReader[Boolean] = ColReader { (col, row) => row.boolOption(col)}
  implicit val byteArrayReader: ColReader[Array[Byte]] = ColReader { (col, row) => row.byteArrayOption(col)}
  implicit val byteReader: ColReader[Byte] = ColReader { (col, row) => row.byteOption(col)}
  implicit val dateReader: ColReader[Date] = ColReader { (col, row) => row.dateOption(col)}
  implicit val localDateReader: ColReader[LocalDate] = ColReader { (col, row) => row.localDateOption(col)}
  implicit val instantReader: ColReader[Instant] = ColReader { (col, row) => row.instantOption(col)}
  implicit val doubleReader: ColReader[Double] = ColReader { (col, row) => row.doubleOption(col)}
  implicit val intReader: ColReader[Int] = ColReader { (col, row) => row.intOption(col)}
  implicit val longReader: ColReader[Long] = ColReader { (col, row) => row.longOption(col)}
  implicit val shortReader: ColReader[Short] = ColReader { (col, row) => row.shortOption(col)}
  implicit val stringReader: ColReader[String] = ColReader { (col, row) => row.stringOption(col)}
  implicit val uuidReader: ColReader[UUID] = ColReader[UUID] { (col, row) => row.uuidOption(col)}

  def enumReader[A <: Enumeration](e: A): ColReader[e.Value] = {
    intReader.flatMap(id => ColReader[e.Value] { (_, _) =>
      e.values.find(_.id == id)
    })
  }
}
