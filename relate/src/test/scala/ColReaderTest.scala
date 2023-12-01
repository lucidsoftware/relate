package com.lucidchart.relate

import org.specs2.mutable._
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any

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

object RecordA {
  implicit val reader: RowParser[RecordA] = new RowParser[RecordA] {
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
    val row = mock(classOf[MockableRow])
    when(row.apply(any())(any())).thenCallRealMethod()
    when(row.opt(any())(any())).thenCallRealMethod()
    when(row.bigDecimalOption("bd")).thenReturn(Some(BigDecimal(10)))
    when(row.bigIntOption("bi")).thenReturn(Some(BigInt(10)))
    when(row.javaBigDecimalOption("jbd")).thenReturn(Some(new java.math.BigDecimal(10)))
    when(row.javaBigIntegerOption("jbi")).thenReturn(Some(java.math.BigInteger.valueOf(10)))
    when(row.boolOption("bool")).thenReturn(Some(true))
    when(row.byteArrayOption("ba")).thenReturn(Some(Array[Byte](1, 2, 3)))
    when(row.byteOption("byte")).thenReturn(Some(1: Byte))
    when(row.dateOption("date")).thenReturn(Some(new Date(timeMillis)))
    when(row.instantOption("instant")).thenReturn(Some(Instant.ofEpochMilli(timeMillis)))
    when(row.doubleOption("double")).thenReturn(Some(1.1))
    when(row.intOption("int")).thenReturn(Some(10))
    when(row.longOption("long")).thenReturn(Some(100L))
    when(row.shortOption("short")).thenReturn(Some(5: Short))
    when(row.stringOption("str")).thenReturn(Some("hello"))
    when(row.uuidOption("uuid")).thenReturn(Some(uuid))
    when(row.intOption("thing")).thenReturn(Some(1))
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

object RecordB {
  implicit val reader: RowParser[RecordB] = new RowParser[RecordB] {
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
    val row = mock(classOf[MockableRow])
    when(row.apply(any())(any())).thenCallRealMethod()
    when(row.opt(any())(any())).thenCallRealMethod()
    when(row.bigDecimalOption("bd")).thenReturn(None)
    when(row.bigIntOption("bi")).thenReturn(None)
    when(row.javaBigDecimalOption("jbd")).thenReturn(None)
    when(row.javaBigIntegerOption("jbi")).thenReturn(None)
    when(row.boolOption("bool")).thenReturn(None)
    when(row.byteArrayOption("ba")).thenReturn(None)
    when(row.byteOption("byte")).thenReturn(None)
    when(row.dateOption("date")).thenReturn(None)
    when(row.instantOption("instant")).thenReturn(None)
    when(row.doubleOption("double")).thenReturn(None)
    when(row.intOption("int")).thenReturn(None)
    when(row.longOption("long")).thenReturn(None)
    when(row.shortOption("short")).thenReturn(None)
    when(row.stringOption("str")).thenReturn(None)
    when(row.uuidOption("uuid")).thenReturn(None)
    when(row.intOption("thing")).thenReturn(None)
    row
  }

}

object Things extends Enumeration {
  val One = Value(1)
  val Two = Value(2)

  implicit val colReader: ColReader[Value] = ColReader.enumReader(this)
}

class ColReaderTest extends Specification {
  val mockedInstant = Instant.EPOCH.plusMillis(RecordA.timeMillis)

  "ColReader" should {
    "parse a present values" in {
      val row = RecordA.mockRow
      val parsed = RecordA.reader.parse(row)

      // Arrays use reference equality so we have to check this
      // independently of all the other values
      val bytes = parsed.ba
      bytes === Array[Byte](1, 2, 3)

      parsed must beEqualTo(RecordA(
        bd = BigDecimal(10),
        bigInt = BigInt(10),
        jBigDecimal = new java.math.BigDecimal(10),
        jBigInt = java.math.BigInteger.valueOf(10),
        bool = true,
        ba = parsed.ba,
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
      ))
    }

    "parse Nones when" in {
      val row = RecordB.mockRow
      val parsed = RecordB.reader.parse(row)

      parsed must beEqualTo(RecordB(
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
      ))
    }

    "parse Somes" in {
      val row = RecordA.mockRow
      val parsed = RecordB.reader.parse(row)

      // Arrays use reference equality so we have to check this
      // independantly of all the other values
      val bytes: Array[Byte] = parsed.ba.get
      bytes === Array[Byte](1, 2, 3)

      parsed.copy(ba = None) must beEqualTo(RecordB(
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
      ))
    }
  }

  "uuidReader" should {
    "parse a byte array" in {
      val rs = mock(classOf[java.sql.ResultSet])
      val row = SqlRow(rs)
      when(rs.getObject("col")) thenReturn Array[Byte]('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
        'f')

      ColReader.uuidReader.read("col", row) must beSome(new UUID(3472611983179986487L, 4051376414998685030L))
    }

    "parse a uuid" in {
      val rs = mock(classOf[java.sql.ResultSet])
      val row = SqlRow(rs)
      when(rs.getObject("col")) thenReturn new UUID(3472611983179986487L, 4051376414998685030L)

      ColReader.uuidReader.read("col", row) must beSome(new UUID(3472611983179986487L, 4051376414998685030L))
    }
  }
}
