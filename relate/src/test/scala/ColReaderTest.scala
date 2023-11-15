package com.lucidchart.relate

import org.specs2.mock.Mockito
import org.specs2.mutable._

import java.time.Instant
import java.util.{Date, UUID}

case class RecordA(
  bd: BigDecimal,
  bigInt: BigInt,
  jBigDecimal: java.math.BigDecimal,
  jBigInt: java.math.BigInteger,
  bool: Boolean,
  ba: Array[Byte],
  byte: Byte,
  date: Date,
  instant: Instant,
  double: Double,
  int: Int,
  long: Long,
  short: Short,
  str: String,
  uuid: UUID,
  thing: Things.Value
)

/**
 * Ensures that the real apply/opt methods are called (whereas `mock[SqlRow]` would 'null' out that method
 */
class MockableRow extends SqlRow(null) {
  final override def apply[A: ColReader](col: String): A = super.apply(col)
  final override def opt[A: ColReader](col: String): Option[A] = super.opt(col)
}

object RecordA extends Mockito {
  implicit val reader = new RowParser[RecordA] {
    def parse(row: SqlRow): RecordA = {
      RecordA(
        row[BigDecimal]("bd"),
        row[BigInt]("bi"),
        row[java.math.BigDecimal]("jbd"),
        row[java.math.BigInteger]("jbi"),
        row[Boolean]("bool"),
        row[Array[Byte]]("ba"),
        row[Byte]("byte"),
        row[Date]("date"),
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

  val timeMillis: Long = 1576179411000L
  val uuid: UUID = UUID.fromString("01020304-0506-0708-090a-0b0c0d0e0f10")

  val mockRow = {
    val row = mock[MockableRow]
    row.bigDecimalOption("bd") returns Some(BigDecimal(10))
    row.bigIntOption("bi") returns Some(BigInt(10))
    row.javaBigDecimalOption("jbd") returns Some(new java.math.BigDecimal(10))
    row.javaBigIntegerOption("jbi") returns Some(java.math.BigInteger.valueOf(10))
    row.boolOption("bool") returns Some(true)
    row.byteArrayOption("ba") returns Some(Array[Byte](1, 2, 3))
    row.byteOption("byte") returns Some(1: Byte)
    row.dateOption("date") returns Some(new Date(timeMillis))
    row.instantOption("instant") returns Some(Instant.ofEpochMilli(timeMillis))
    row.doubleOption("double") returns Some(1.1)
    row.intOption("int") returns Some(10)
    row.longOption("long") returns Some(100L)
    row.shortOption("short") returns Some(5: Short)
    row.stringOption("str") returns Some("hello")
    row.uuidOption("uuid") returns Some(uuid)
    row.intOption("thing") returns Some(1)
    row
  }

}

case class RecordB(
  bd: Option[BigDecimal],
  bigInt: Option[BigInt],
  jBigDecimal: Option[java.math.BigDecimal],
  jBigInt: Option[java.math.BigInteger],
  bool: Option[Boolean],
  ba: Option[Array[Byte]],
  byte: Option[Byte],
  date: Option[Date],
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
        row.opt[BigInt]("bi"),
        row.opt[java.math.BigDecimal]("jbd"),
        row.opt[java.math.BigInteger]("jbi"),
        row.opt[Boolean]("bool"),
        row.opt[Array[Byte]]("ba"),
        row.opt[Byte]("byte"),
        row.opt[Date]("date"),
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
    val row = mock[MockableRow]
    row.bigDecimalOption("bd") returns None
    row.bigIntOption("bi") returns None
    row.javaBigDecimalOption("jbd") returns None
    row.javaBigIntegerOption("jbi") returns None
    row.boolOption("bool") returns None
    row.byteArrayOption("ba") returns None
    row.byteOption("byte") returns None
    row.dateOption("date") returns None
    row.instantOption("instant") returns None
    row.doubleOption("double") returns None
    row.intOption("int") returns None
    row.longOption("long") returns None
    row.shortOption("short") returns None
    row.stringOption("str") returns None
    row.uuidOption("uuid") returns None
    row.intOption("thing") returns None
    row
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
      // independently of all the other values
      val bytes = parsed.ba
      bytes === Array[Byte](1, 2, 3)

      parsed.copy(ba = null) mustEqual RecordA(
        bd = BigDecimal(10),
        bigInt = BigInt(10),
        jBigDecimal = new java.math.BigDecimal(10),
        jBigInt = java.math.BigInteger.valueOf(10),
        bool = true,
        ba = null,
        byte = 1,
        date = new Date(mockedInstant.toEpochMilli),
        instant = mockedInstant,
        double = 1.1,
        int = 10,
        long = 100,
        short = 5,
        str = "hello",
        uuid = RecordA.uuid,
        thing = Things.One
      )
    }

    "parse Nones when" in {
      val row = RecordB.mockRow
      val parsed = RecordB.reader.parse(row)

      parsed mustEqual RecordB(
        bd = None,
        bigInt = None,
        jBigDecimal = None,
        jBigInt = None,
        bool = None,
        ba = None,
        byte = None,
        date = None,
        instant = None,
        double = None,
        int = None,
        long = None,
        short = None,
        str = None,
        uuid = None,
        thing = None
      )
    }

    "parse Somes" in {
      val row = RecordA.mockRow
      val parsed = RecordB.reader.parse(row)

      // Arrays use reference equality so we have to check this
      // independantly of all the other values
      val bytes: Array[Byte] = parsed.ba.get
      bytes === Array[Byte](1, 2, 3)

      parsed.copy(ba = None) mustEqual RecordB(
        bd = Some(BigDecimal(10)),
        bigInt = Some(BigInt(10)),
        jBigDecimal = Some(new java.math.BigDecimal(10)),
        jBigInt = Some(java.math.BigInteger.valueOf(10)),
        bool = Some(true),
        ba = None,
        byte = Some(1),
        date = Some(new Date(mockedInstant.toEpochMilli)),
        instant = Some(mockedInstant),
        double = Some(1.1),
        int = Some(10),
        long = Some(100),
        short = Some(5),
        str = Some("hello"),
        uuid = Some(UUID.fromString("01020304-0506-0708-090a-0b0c0d0e0f10")),
        thing = Some(Things.One)
      )
    }
  }

  "uuidReader" should {
    "parse a byte array" in {
      val rs = mock[java.sql.ResultSet]
      val row = SqlRow(rs)
      rs.getObject("col") returns Array[Byte]('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
        'f')

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
