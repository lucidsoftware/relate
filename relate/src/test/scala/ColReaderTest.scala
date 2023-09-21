package com.lucidchart.relate

import java.sql.Timestamp
import java.time.Instant
import java.util.{Date, UUID}
import org.specs2.mock.Mockito
import org.specs2.mutable._

case class RecordA(
  bd: BigDecimal,
  bool: Boolean,
  ba: Array[Byte],
  byte: Byte,
  sqlDate: java.sql.Date,
  timestampDate: Date,
  timestamp: Timestamp,
  instant: Instant,
  double: Double,
  int: Int,
  long: Long,
  short: Short,
  str: String,
  uuid: UUID,
  thing: Things.Value
)

object RecordA extends Mockito {
  implicit val reader = new RowParser[RecordA] {
    def parse(row: SqlRow): RecordA = {
      RecordA(
        row[BigDecimal]("bd"),
        row[Boolean]("bool"),
        row[Array[Byte]]("ba"),
        row[Byte]("byte"),
        row[java.sql.Date]("sqlDate"),
        row[Date]("timestampDate"),
        row[Timestamp]("timestamp"),
        row[Instant]("instant"),
        row[Double]("double"),
        row[Int]("int"),
        row[Long]("long"),
        row[Short]("short"),
        row[String]("str"),
        row[UUID]("uuid"),
        row[Things.Value]("thing")
      )
    }
  }

  val timeMillis: Long = 1576179411000l

  val mockRow = {
    val rs = mock[java.sql.ResultSet]
    rs.getBigDecimal("bd") returns new java.math.BigDecimal(10)
    rs.getBoolean("bool") returns true
    rs.getBytes("ba") returns Array[Byte](1,2,3)
    rs.getByte("byte") returns (1: Byte)
    rs.getDate("sqlDate") returns (new java.sql.Date(10000))
    rs.getTimestamp("timestampDate") returns (new Timestamp(timeMillis))
    rs.getTimestamp("timestamp") returns (new Timestamp(timeMillis))
    rs.getTimestamp("instant") returns (new java.sql.Timestamp(timeMillis))
    rs.getDouble("double") returns 1.1
    rs.getInt("int") returns 10
    rs.getLong("long") returns 100L
    rs.getShort("short") returns (5: Short)
    rs.getString("str") returns "hello"
    rs.getObject("uuid") returns Array[Byte](1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16)
    rs.getInt("thing") returns 1
    SqlRow(rs)
  }

}

case class RecordB(
  bd: Option[BigDecimal],
  bool: Option[Boolean],
  ba: Option[Array[Byte]],
  byte: Option[Byte],
  sqlDate: Option[java.sql.Date],
  timestampDate: Option[Date],
  timestamp: Option[Timestamp],
  instant: Option[Instant],
  double: Option[Double],
  int: Option[Int],
  long: Option[Long],
  short: Option[Short],
  str: Option[String],
  uuid: Option[UUID],
  thing: Option[Things.Value]
)

object RecordB extends Mockito {
  implicit val reader = new RowParser[RecordB] {
    def parse(row: SqlRow): RecordB = {
      RecordB(
        row.opt[BigDecimal]("bd"),
        row.opt[Boolean]("bool"),
        row.opt[Array[Byte]]("ba"),
        row.opt[Byte]("byte"),
        row.opt[java.sql.Date]("sqlDate"),
        row.opt[Date]("timestampDate"),
        row.opt[Timestamp]("timestamp"),
        row.opt[Instant]("instant"),
        row.opt[Double]("double"),
        row.opt[Int]("int"),
        row.opt[Long]("long"),
        row.opt[Short]("short"),
        row.opt[String]("str"),
        row.opt[UUID]("uuid"),
        row.opt[Things.Value]("thing")
      )
    }
  }

  val mockRow = {
    val rs = mock[java.sql.ResultSet]
    rs.wasNull() returns true
    rs.getBigDecimal("bd") returns null
    rs.getBytes("ba") returns null
    rs.getDate("sqlDate") returns null
    rs.getTimestamp("timestampDate") returns null
    rs.getTimestamp("timestamp") returns null
    rs.getString("str") returns null
    rs.getBytes("uuid") returns null
    SqlRow(rs)
  }

}

object Things extends Enumeration {
  val One = Value(1)
  val Two = Value(2)

  implicit val colReader: ColReader[Value] = ColReader.enumReader(this)
}

class ColReaderTest extends Specification with Mockito {
  val mockedInstant = Instant.EPOCH.plusMillis(RecordA.timeMillis)
  "ColReader" should {
    "parse a present values" in {
      val row = RecordA.mockRow
      val parsed = RecordA.reader.parse(row)

      // Arrays use reference equality so we have to check this
      // independantly of all the other values
      val bytes = parsed.ba
      bytes === Array[Byte](1,2,3)

      parsed.copy(ba = null) mustEqual RecordA(
        BigDecimal(10),
        true,
        null,
        1,
        new java.sql.Date(10000),
        new Date(mockedInstant.toEpochMilli),
        new Timestamp(mockedInstant.toEpochMilli),
        mockedInstant,
        1.1,
        10,
        100,
        5,
        "hello",
        UUID.fromString("01020304-0506-0708-090a-0b0c0d0e0f10"),
        Things.One
      )
    }

    "parse Nones when" in {
      val row = RecordB.mockRow
      val parsed = RecordB.reader.parse(row)

      parsed mustEqual RecordB(
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None
      )
    }

    "parse Somes" in {
      val row = RecordA.mockRow
      val parsed = RecordB.reader.parse(row)

      // Arrays use reference equality so we have to check this
      // independantly of all the other values
      val bytes = parsed.ba.get
      bytes === Array[Byte](1,2,3)

      parsed.copy(ba = null) mustEqual RecordB(
        Some(BigDecimal(10)),
        Some(true),
        null,
        Some(1),
        Some(new java.sql.Date(10000)),
        Some(new Date(mockedInstant.toEpochMilli)),
        Some(new Timestamp(mockedInstant.toEpochMilli)),
        Some(mockedInstant),
        Some(1.1),
        Some(10),
        Some(100),
        Some(5),
        Some("hello"),
        Some(UUID.fromString("01020304-0506-0708-090a-0b0c0d0e0f10")),
        Some(Things.One)
      )
    }
  }

  "uuidReader" should {
    "parse a byte array" in {
      val rs = mock[java.sql.ResultSet]
      val row = SqlRow(rs)
      rs.getObject("col") returns Array[Byte]('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

      ColReader.uuidReader.read("col", row) mustEqual Some(new UUID(3472611983179986487L, 4051376414998685030L))
    }

    "parse a uuid" in {
      val rs = mock[java.sql.ResultSet]
      val row = SqlRow(rs)
      rs.getObject("col") returns new UUID(3472611983179986487L, 4051376414998685030L)

      ColReader.uuidReader.read("col", row) mustEqual Some(new UUID(3472611983179986487L, 4051376414998685030L))
    }
  }
}
