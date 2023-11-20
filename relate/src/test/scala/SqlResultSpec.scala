package com.lucidchart.relate

import com.lucidchart.relate.SqlResultTypes._
import java.io.{ByteArrayInputStream, Reader}
import java.net.URL
import java.sql.{Blob, Clob, Connection, NClob, Ref, RowId, SQLXML, Time, Timestamp}
import java.time.Instant
import java.util.{Calendar, Date, UUID}
import org.specs2.mutable._
import org.specs2.matcher._
// currently use vanilla mockito, because the scala libraries don't support scala 3
import org.mockito.Mockito.*
import scala.jdk.CollectionConverters._

case class TestRecord(
  id: Long,
  name: String
)

object TestRecord {
  implicit val TestRecordRowParser: RowParser[TestRecord] = new RowParser[TestRecord] {
    def parse(row: SqlRow): TestRecord = TestRecord(
      row.long("id"),
      row.string("name")
    )
  }
}

class SqlResultSpec extends Specification {
  val parser = { implicit row: SqlRow =>
    TestRecord(
      long("id"),
      string("name")
    )
  }

  val pairparser = { implicit row: SqlRow =>
    val id = long("id")
    id -> TestRecord(
      id,
      string("name")
    )
  }

  def getMocks = {
    val rs = mock(classOf[java.sql.ResultSet])
    (rs, SqlRow(rs), SqlResult(rs))
  }

  implicit val con: Connection = null

  "asSingle" should {
    "return a single row with an explicit parser" in {
      val (rs, _, result) = getMocks

      when(rs.getRow).thenReturn(0).thenReturn(1)
      when(rs.next).thenReturn(true).thenReturn(false)
      when(rs.getLong("id")).thenReturn(100L)
      when(rs.getString("name")).thenReturn("the name")

      result.asSingle(parser) must beEqualTo(TestRecord(100L, "the name"))
    }

    "return a single row with an implicit parser" in {
      val (rs, _, result) = getMocks

      when(rs.getRow).thenReturn(0).thenReturn(1)
      when(rs.next).thenReturn(true).thenReturn(false)
      when(rs.getLong("id")).thenReturn(100L)
      when(rs.getString("name")).thenReturn("the name")

      result.asSingle[TestRecord] must beEqualTo(TestRecord(100L, "the name"))
    }
  }

  "asSingleOption" should {
    def init(rs: java.sql.ResultSet, next: Boolean) = {
      when(rs.getRow).thenReturn(0).thenReturn(1)
      when(rs.next).thenReturn(next)
      when(rs.getLong("id")).thenReturn(100L)
      when(rs.getString("name")).thenReturn("the name")
    }

    "return a single row with an explicit parser" in {
      val (rs, _, result) = getMocks
      init(rs, true)

      result.asSingleOption(parser) must beSome(TestRecord(100L, "the name"))
    }

    "return a None with an explicit parser" in {
      val (rs, _, result) = getMocks
      init(rs, false)

      result.asSingleOption(parser) must beNone
    }

    "return a single row with an implicit parser" in {
      val (rs, _, result) = getMocks
      init(rs, true)

      result.asSingleOption[TestRecord] must beSome(TestRecord(100L, "the name"))
    }

    "return a None with an implicit parser" in {
      val (rs, _, result) = getMocks
      init(rs, false)

      result.asSingleOption[TestRecord] must beNone
    }
  }

  "asList" should {
    "return a list of 3 elements with an explicit parser" in {
      val (rs, _, result) = getMocks

      when(rs.getRow).thenReturn(0) thenReturn 1 thenReturn 2 thenReturn 3
      when(rs.next).thenReturn(true) thenReturn true thenReturn true thenReturn false
      when(rs.getLong("id")).thenReturn(100L: java.lang.Long)
      when(rs.getString("name")).thenReturn("the name")

      result.asList(parser) must beEqualTo(List(
        TestRecord(100L, "the name"),
        TestRecord(100L, "the name"),
        TestRecord(100L, "the name")
      ))
    }

    "return an empty list with an explicit parser" in {
      val (rs, _, result) = getMocks

      when(rs.getRow).thenReturn(0)
      when(rs.next).thenReturn(false)

      result.asList(parser) must beEqualTo(List())
    }

    "return a list of 3 elements with an implicit parser" in {
      val (rs, _, result) = getMocks

      when(rs.getRow).thenReturn(0) thenReturn 1 thenReturn 2 thenReturn 3
      when(rs.next).thenReturn(true) thenReturn true thenReturn true thenReturn false
      when(rs.getLong("id")).thenReturn(100L: java.lang.Long)
      when(rs.getString("name")).thenReturn("the name")

      result.asList[TestRecord] must beEqualTo(List(
        TestRecord(100L, "the name"),
        TestRecord(100L, "the name"),
        TestRecord(100L, "the name")
      ))
    }

    "return an empty list with an implicit parser" in {
      val (rs, _, result) = getMocks

      when(rs.getRow).thenReturn(0)
      when(rs.next).thenReturn(false)

      result.asList[TestRecord] must beEqualTo(List())
    }
  }

  "asMap" should {
    "return a map of 3 elements with an explicit parser" in {
      val (rs, _, result) = getMocks

      when(rs.getRow).thenReturn(0) thenReturn 1 thenReturn 2 thenReturn 3
      when(rs.next).thenReturn(true) thenReturn true thenReturn true thenReturn false
      when(rs.getLong("id")).thenReturn(1L).thenReturn(2L).thenReturn(3L)
      when(rs.getString("name")).thenReturn("the name")

      val res = result.asMap(pairparser)
      res(1L) must beEqualTo(TestRecord(1L, "the name"))
      res(2L) must beEqualTo(TestRecord(2L, "the name"))
      res(3L) must beEqualTo(TestRecord(3L, "the name"))
    }

    "return an empty map with an explicit parser" in {
      val (rs, _, result) = getMocks
      when(rs.getRow).thenReturn(0)
      when(rs.next).thenReturn(false)
      result.asMap(pairparser) must beEqualTo(Map())
    }

    implicit val a: RowParser[(Long, TestRecord)] = new RowParser[(Long, TestRecord)] {
      def parse(row: SqlRow) = {
        val id = row.long("id")
        id -> TestRecord(id, row.string("name"))
      }
    }

    "return a map of 3 elements with an implicit parser" in {
      val (rs, _, result) = getMocks
      import java.lang.{Long => L}

      when(rs.getRow).thenReturn(0) thenReturn 1 thenReturn 2 thenReturn 3
      when(rs.next).thenReturn(true) thenReturn true thenReturn true thenReturn false
      when(rs.getLong("id")).thenReturn(1: L).thenReturn(2: L).thenReturn(3: L)
      when(rs.getString("name")).thenReturn("the name")

      val res = result.asMap[Long, TestRecord]
      res(1L) must beEqualTo(TestRecord(1L, "the name"))
      res(2L) must beEqualTo(TestRecord(2L, "the name"))
      res(3L) must beEqualTo(TestRecord(3L, "the name"))
    }

    "return an empty map with an implicit parser" in {
      val (rs, _, result) = getMocks
      when(rs.getRow).thenReturn(0)
      when(rs.next).thenReturn(false)
      result.asMap[Long, TestRecord] must beEqualTo(Map())
    }
  }

  "asMultiMap" should {
    "return a multimap of 2 keys with 2 entries in each" in {
      val (rs, _, result) = getMocks

      when(rs.getRow).thenReturn(0) thenReturn 1 thenReturn 2 thenReturn 3 thenReturn 4
      when(rs.next).thenReturn(true) thenReturn true thenReturn true thenReturn true thenReturn false
      when(rs.getString("id")).thenReturn("1") thenReturn "2" thenReturn "1" thenReturn "2"
      when(rs.getString("name")).thenReturn("one") thenReturn "two" thenReturn "three" thenReturn "four"

      val res = result.asMultiMap { row =>
        row.string("id") -> row.string("name")
      }
      res.keys must containTheSameElementsAs(Seq("1", "2"))
      res("1") must containTheSameElementsAs(Seq("one", "three"))
      res("2") must containTheSameElementsAs(Seq("two", "four"))
    }
  }

  "scalar" should {
    "return the correct type" in {
      val (rs, _, result) = getMocks

      when(rs.next).thenReturn(true)
      when(rs.getObject(1)).thenReturn(2: java.lang.Long)

      result.asScalar[Long] must beEqualTo(2L)
    }

    "ignore other result values" in {
      val (rs, _, result) = getMocks

      when(rs.next).thenReturn(true)
      when(rs.getObject(1)).thenReturn("test")
      when(rs.getObject(2)).thenReturn(2L: java.lang.Long)

      result.asScalar[String] must beEqualTo("test")
    }

    "return null if there are no rows" in {
      val (rs, _, result) = getMocks

      when(rs.next).thenReturn(false)

      result.asScalarOption[Long] must beEqualTo(None)
    }
  }

  "extractOption" should {
    "Extract a Some" in {
      val (rs, row, _) = getMocks

      val name: Object = "hello"
      when(rs.getObject("name")).thenReturn(name)

      val id: Object = 12: java.lang.Integer
      when(rs.getObject("id")).thenReturn(id)

      val nameOpt = row.extractOption("name") { any =>
        any match {
          case x: String => x
          case _         => ""
        }
      }

      nameOpt must beSome("hello")

      val idOpt = row.extractOption("id") { any =>
        any match {
          case x: Int => x
          case _      => 0
        }
      }

      idOpt must beSome(12)
    }

    "Extract a None" in {
      val (rs, row, _) = getMocks

      when(rs.getObject("null")).thenReturn(null)

      val nullOpt = row.extractOption("null") { _ => "hello" }

      nullOpt must beNone
    }
  }

  "getRow" should {
    "return current row number of a ResultSet" in {
      val (rs, row, _) = getMocks

      when(rs.getRow()).thenReturn(3)
      row.getRow() must beEqualTo(3)
    }
  }

  "wasNull" should {
    "return true if the last read was null" in {
      val (rs, _, result) = getMocks

      when(rs.wasNull()).thenReturn(true)
      result.wasNull() must beEqualTo(true)
    }
  }

  "strictArray" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val arr = mock(classOf[java.sql.Array])
      when(rs.getArray("array")).thenReturn(arr)
      row.strictArray("array") must beEqualTo(arr)
      row.strictArrayOption("array") must beSome(arr)
    }
  }

  "strictAsciiStream" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val stream = new ByteArrayInputStream("hello".getBytes())
      when(rs.getAsciiStream("ascii-stream")).thenReturn(stream)
      row.strictAsciiStream("ascii-stream") must beEqualTo(stream)
      row.strictAsciiStreamOption("ascii-stream") must beSome(stream)
    }
  }

  "strictBigDecimal" should {
    "properly pass through the call to ResultSet and returns a scala.math.BigDecimal" in {
      val (rs, row, _) = getMocks

      when(rs.getBigDecimal("big-decimal")).thenReturn(new java.math.BigDecimal("100.9999"))
      row.strictBigDecimal("big-decimal") must beEqualTo(BigDecimal("100.9999"))
      row.strictBigDecimalOption("big-decimal") must beSome(BigDecimal("100.9999"))
    }
  }

  "strictBinaryStream" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val stream = new ByteArrayInputStream("hello".getBytes())
      when(rs.getBinaryStream("binary-stream")).thenReturn(stream)
      row.strictBinaryStream("binary-stream") must beEqualTo(stream)
      row.strictBinaryStreamOption("binary-stream") must beSome(stream)
    }
  }

  "strictBlob" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val blob = mock(classOf[java.sql.Blob])
      when(rs.getBlob("blob")).thenReturn(blob)
      row.strictBlob("blob") must beEqualTo(blob)
      row.strictBlobOption("blob") must beSome(blob)
    }
  }

  "strictBoolean" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = true
      when(rs.getBoolean("strictBoolean")).thenReturn(res)
      row.strictBoolean("strictBoolean") must beEqualTo(res)
      row.strictBooleanOption("strictBoolean") must beSome(res)
    }
  }

  "strictByte" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res: Byte = 1
      when(rs.getByte("strictByte")).thenReturn(res)
      row.strictByte("strictByte") must beEqualTo(res)
      row.strictByteOption("strictByte") must beSome(res)
    }
  }

  "strictBytes" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res: Array[Byte] = Array(1, 2, 3)
      when(rs.getBytes("strictBytes")).thenReturn(res)
      row.strictBytes("strictBytes") must beEqualTo(res)
      row.strictBytesOption("strictBytes") must beSome(res)
    }
  }

  "strictCharacterStream" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = mock(classOf[Reader])
      when(rs.getCharacterStream("strictCharacterStream")).thenReturn(res)
      row.strictCharacterStream("strictCharacterStream") must beEqualTo(res)
      row.strictCharacterStreamOption("strictCharacterStream") must beSome(res)
    }
  }

  "strictClob" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = mock(classOf[Clob])
      when(rs.getClob("strictClob")).thenReturn(res)
      row.strictClob("strictClob") must beEqualTo(res)
      row.strictClobOption("strictClob") must beSome(res)
    }
  }

  "strictDate" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = mock(classOf[java.sql.Date])
      when(rs.getDate("strictDate")).thenReturn(res)
      row.strictDate("strictDate") must beEqualTo(res)
      row.strictDateOption("strictDate") must beSome(res)

      val cal = Calendar.getInstance()
      when(rs.getDate("strictDate", cal)).thenReturn(res)
      row.strictDate("strictDate", cal) must beEqualTo(res)
      row.strictDateOption("strictDate", cal) must beSome(res)
    }
  }

  "strictDouble" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res: Double = 1.1
      when(rs.getDouble("strictDouble")).thenReturn(res)
      row.strictDouble("strictDouble") must beEqualTo(res)
      row.strictDoubleOption("strictDouble") must beSome(res)
    }
  }

  "strictFloat" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = 1.1f
      when(rs.getFloat("strictFloat")).thenReturn(res)
      row.strictFloat("strictFloat") must beEqualTo(res)
      row.strictFloatOption("strictFloat") must beSome(res)
    }
  }

  "strictInt" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = 1
      when(rs.getInt("strictInt")).thenReturn(res)
      row.strictInt("strictInt") must beEqualTo(res)
      row.strictIntOption("strictInt") must beSome(res)
    }
  }

  "strictIntOption" should {
    "return None when the value in the database is null" in {
      val (rs, row, _) = getMocks

      val res = 0
      when(rs.getInt("strictInt")).thenReturn(res)
      when(rs.wasNull).thenReturn(true)
      row.strictIntOption("strictInt") must beNone
    }

    "return Some when the value in the database is not null" in {
      val (rs, row, _) = getMocks

      val res = 0
      when(rs.getInt("strictInt")).thenReturn(res)
      when(rs.wasNull).thenReturn(false)
      row.strictIntOption("strictInt") must beSome(res)
    }
  }

  "strictLong" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = 1000L
      when(rs.getLong("strictLong")).thenReturn(res)
      row.strictLong("strictLong") must beEqualTo(res)
      row.strictLongOption("strictLong") must beSome(res)
    }
  }

  "strictNCharacterStream" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = mock(classOf[Reader])
      when(rs.getNCharacterStream("strictNCharacterStream")).thenReturn(res)
      row.strictNCharacterStream("strictNCharacterStream") must beEqualTo(res)
      row.strictNCharacterStreamOption("strictNCharacterStream") must beSome(res)
    }
  }

  "strictNClob" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = mock(classOf[NClob])
      when(rs.getNClob("strictNClob")).thenReturn(res)
      row.strictNClob("strictNClob") must beEqualTo(res)
      row.strictNClobOption("strictNClob") must beSome(res)
    }
  }

  "strictNString" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = "hello"
      when(rs.getNString("strictNString")).thenReturn(res)
      row.strictNString("strictNString") must beEqualTo(res)
      row.strictNStringOption("strictNString") must beSome(res)
    }
  }

  "strictObject" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res: Object = 123: java.lang.Integer
      when(rs.getObject("strictObject")).thenReturn(res)
      row.strictObject("strictObject") must beEqualTo(res)
      row.strictObjectOption("strictObject") must beSome(res)

      val map = Map[String, Class[_]]()
      when(rs.getObject("strictObject", map.asJava)).thenReturn(res)
      row.strictObject("strictObject", map) must beEqualTo(res)
      row.strictObjectOption("strictObject", map) must beSome(res)
    }
  }

  "strictRef" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = mock(classOf[Ref])
      when(rs.getRef("strictRef")).thenReturn(res)
      row.strictRef("strictRef") must beEqualTo(res)
      row.strictRefOption("strictRef") must beSome(res)
    }
  }

  "strictRowId" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = mock(classOf[RowId])
      when(rs.getRowId("strictRowId")).thenReturn(res)
      row.strictRowId("strictRowId") must beEqualTo(res)
      row.strictRowIdOption("strictRowId") must beSome(res)
    }
  }

  "strictShort" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res: Short = 1
      when(rs.getShort("strictShort")).thenReturn(res)
      row.strictShort("strictShort") must beEqualTo(res)
      row.strictShortOption("strictShort") must beSome(res)
    }
  }

  "strictSQLXML" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = mock(classOf[SQLXML])
      when(rs.getSQLXML("strictSQLXML")).thenReturn(res)
      row.strictSQLXML("strictSQLXML") must beEqualTo(res)
      row.strictSQLXMLOption("strictSQLXML") must beSome(res)
    }
  }

  "strictString" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = "hello"
      when(rs.getString("strictString")).thenReturn(res)
      row.strictString("strictString") must beEqualTo(res)
      row.strictStringOption("strictString") must beSome(res)
    }
  }

  "strictTime" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = mock(classOf[Time])
      when(rs.getTime("strictTime")).thenReturn(res)
      row.strictTime("strictTime") must beEqualTo(res)
      row.strictTimeOption("strictTime") must beSome(res)

      val cal = Calendar.getInstance()
      when(rs.getTime("strictTime", cal)).thenReturn(res)
      row.strictTime("strictTime", cal) must beEqualTo(res)
      row.strictTimeOption("strictTime", cal) must beSome(res)
    }
  }

  "strictTimestamp" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = mock(classOf[Timestamp])
      when(rs.getTimestamp("strictTimestamp")).thenReturn(res)
      row.strictTimestamp("strictTimestamp") must beEqualTo(res)
      row.strictTimestampOption("strictTimestamp") must beSome(res)

      val cal = Calendar.getInstance()
      when(rs.getTimestamp("strictTimestamp", cal)).thenReturn(res)
      row.strictTimestamp("strictTimestamp", cal) must beEqualTo(res)
      row.strictTimestampOption("strictTimestamp", cal) must beSome(res)
    }
  }

  "strictURL" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = new URL("http://localhost")
      when(rs.getURL("strictURL")).thenReturn(res)
      row.strictURL("strictURL") must beEqualTo(res)
      row.strictURLOption("strictURL") must beSome(res)
    }
  }

  "string" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val res = "hello"
      when(rs.getString("string")).thenReturn(res)
      row.string("string") must beEqualTo(res)
      row.stringOption("string") must beSome(res)
    }
  }

  "int" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val res = 10: java.lang.Integer
      when(rs.getInt("int")).thenReturn(res)
      row.int("int") must beEqualTo(res)
      row.intOption("int") must beSome(res)
    }
  }

  "intOption" should {
    "return None if the value in the database is null" in {
      val (rs, row, _) = getMocks

      when(rs.wasNull).thenReturn(true)
      row.intOption("int") must beNone
    }

    "return Some(0) if the value in the database was really 0" in {
      val (rs, row, _) = getMocks

      val res = 0: java.lang.Integer
      when(rs.getInt("int")).thenReturn(res)
      when(rs.wasNull).thenReturn(false)
      row.intOption("int") must beSome(res)
    }
  }

  "double" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val res = 1.1: java.lang.Double
      when(rs.getDouble("double")).thenReturn(res)
      row.double("double") must beEqualTo(res)
      row.doubleOption("double") must beSome(res)
    }
  }

  "short" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val res: Short = 1
      when(rs.getShort("short")).thenReturn(res)
      row.short("short") must beEqualTo(res)
      row.shortOption("short") must beSome(res)
    }
  }

  "byte" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val res: Byte = 1
      when(rs.getByte("byte")).thenReturn(res)
      row.byte("byte") must beEqualTo(res)
      row.byteOption("byte") must beSome(res)
    }
  }

  "boolean" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val res = true
      when(rs.getBoolean("boolean")).thenReturn(res)
      row.bool("boolean") must beEqualTo(res)
      row.boolOption("boolean") must beSome(res)
    }
  }

  "long" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val res: java.lang.Long = 100000L
      when(rs.getLong("long")).thenReturn(res)
      row.long("long") must beEqualTo(res)
      row.longOption("long") must beSome(res)
    }
  }

  "bigInt" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val number = 1010101

      val int: Object = number.toInt: java.lang.Integer
      when(rs.getObject("bigInt")).thenReturn(int)
      row.bigInt("bigInt") must beEqualTo(BigInt(number))
      row.bigIntOption("bigInt") must beSome(BigInt(number))

      val long: Object = number.toLong: java.lang.Long
      when(rs.getObject("bigInt")).thenReturn(long)
      row.bigInt("bigInt") must beEqualTo(BigInt(number))
      row.bigIntOption("bigInt") must beSome(BigInt(number))

      val string: Object = number.toString
      when(rs.getObject("bigInt")).thenReturn(string)
      row.bigInt("bigInt") must beEqualTo(BigInt(number))
      row.bigIntOption("bigInt") must beSome(BigInt(number))

      val bigint: Object = new java.math.BigInteger(number.toString)
      when(rs.getObject("bigInt")).thenReturn(bigint)
      row.bigInt("bigInt") must beEqualTo(BigInt(number))
      row.bigIntOption("bigInt") must beSome(BigInt(number))
    }
  }

  "bigDecimal" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val number = 1.013

      val bigint = new java.math.BigDecimal(number.toString)
      when(rs.getBigDecimal("bigDecimal")).thenReturn(bigint)
      row.bigDecimal("bigDecimal") must beEqualTo(BigDecimal(number))
      row.bigDecimalOption("bigDecimal") must beSome(BigDecimal(number))
    }
  }

  "javaBigInteger" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val number = 1010101
      val bigNumber = java.math.BigInteger.valueOf(number)

      val int: Object = number.toInt: java.lang.Integer
      when(rs.getObject("javaBigInteger")).thenReturn(int)
      row.javaBigInteger("javaBigInteger") must beEqualTo(bigNumber)
      row.javaBigIntegerOption("javaBigInteger") must beSome(bigNumber)

      val long: Object = number.toLong: java.lang.Long
      when(rs.getObject("javaBigInteger")).thenReturn(long)
      row.javaBigInteger("javaBigInteger") must beEqualTo(bigNumber)
      row.javaBigIntegerOption("javaBigInteger") must beSome(bigNumber)

      val bigint: Object = new java.math.BigInteger(number.toString)
      when(rs.getObject("javaBigInteger")).thenReturn(bigint)
      row.javaBigInteger("javaBigInteger") must beEqualTo(bigNumber)
      row.javaBigIntegerOption("javaBigInteger") must beSome(bigNumber)

      val str: Object = number.toString
      when(rs.getObject("javaBigInteger")).thenReturn(str)
      row.javaBigInteger("javaBigInteger") must beEqualTo(bigNumber)
      row.javaBigIntegerOption("javaBigInteger") must beSome(bigNumber)
    }
  }

  "javaBigDecimal" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val number = 1

      val bigdec = java.math.BigDecimal.valueOf(number)
      when(rs.getBigDecimal("javaBigDecimal")).thenReturn(bigdec)
      row.javaBigDecimal("javaBigDecimal") must beEqualTo(new java.math.BigDecimal(number.toString))
      row.javaBigDecimalOption("javaBigDecimal") must beSome(new java.math.BigDecimal(number.toString))
    }
  }

  "date" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val now = Instant.now
      val timestamp = Timestamp.from(now)
      val date = Date.from(now)
      // Note that
      timestamp must not(beEqualTo(date))
      date must beEqualTo(timestamp)

      when(rs.getTimestamp("date")).thenReturn(timestamp)
      row.date("date") must beEqualTo(timestamp)
      row.date("date") must beEqualTo(date)
      row.dateOption("date") must beSome(timestamp)
    }
  }

  "instant" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val res = Instant.EPOCH.plusSeconds(10000)
      val timestamp = mock(classOf[Timestamp])
      when(timestamp.toInstant()).thenReturn(res)

      when(rs.getTimestamp("instant")).thenReturn(timestamp)
      row.instant("instant") must beEqualTo(res)
      row.instantOption("instant") must beSome(res)
    }

    "return None if null" in {
      val (_, row, _) = getMocks

      row.instantOption("instant") must beNone
    }
  }

  "byteArray" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val res = Array[Byte]('1', '2', '3')
      when(rs.getObject("byteArray")).thenReturn(res)
      row.byteArray("byteArray") must beEqualTo(res)
      row.byteArrayOption("byteArray") must beSome(res)

      val blob = mock(classOf[Blob])
      when(blob.length).thenReturn(res.length.toLong)
      when(blob.getBytes(0, res.length)).thenReturn(res)
      when(rs.getObject("byteArray")).thenReturn(blob)
      row.byteArray("byteArray") must beEqualTo(res)
      row.byteArrayOption("byteArray") must beSome(res)

      val clob = mock(classOf[Clob])
      when(clob.length).thenReturn(res.length.toLong)
      when(clob.getSubString(1, res.length)).thenReturn("123")
      when(rs.getObject("byteArray")).thenReturn(clob)
      row.byteArray("byteArray") must beEqualTo(res)
      row.byteArrayOption("byteArray") must beSome(res)
    }
  }

  "uuid" should {
    "return the correct value when stored as a byte array" in {
      val (rs, row, _) = getMocks

      val res = Array[Byte]('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
      when(rs.getObject("uuid")).thenReturn(res)
      row.uuid("uuid") must beEqualTo(new UUID(3472611983179986487L, 4051376414998685030L))
      row.uuidOption("uuid") must beSome(new UUID(3472611983179986487L, 4051376414998685030L))
    }

    "return the correct value when stored as UUID" in {
      val (rs, row, _) = getMocks

      val res = new UUID(3472611983179986487L, 4051376414998685030L)
      when(rs.getObject("uuid")).thenReturn(res)
      row.uuid("uuid") must beEqualTo(res)
      row.uuidOption("uuid") must beSome(res)
    }
  }

  "uuidFromString" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val res = "000102030405060708090a0b0c0d0e0f"
      when(rs.getString("uuidFromString")).thenReturn(res)
      row.uuidFromString("uuidFromString") must beEqualTo(new UUID(283686952306183L, 579005069656919567L))
      row.uuidFromStringOption("uuidFromString") must beSome(new UUID(283686952306183L, 579005069656919567L))
    }
  }

  "enum" should {
    object Things extends Enumeration {
      val one = Value(1, "one")
      val two = Value(2, "two")
      val three = Value(3, "three")
    }

    "return the correct value" in {
      val (rs, row, _) = getMocks

      when(rs.getInt("enum")).thenReturn(1) thenReturn 2 thenReturn 3 thenReturn 4
      row.`enum`("enum", Things) must beEqualTo(Things.one)
      row.`enum`("enum", Things) must beEqualTo(Things.two)
      row.enumOption("enum", Things) must beSome(Things.three)
      row.enumOption("enum", Things) must beNone
    }
  }
}
