package com.lucidchart.relate

import com.lucidchart.relate.SqlResultTypes._
import java.io.{ByteArrayInputStream, Reader}
import java.net.URL
import java.sql.{Blob, Clob, Connection, NClob, Ref, RowId, SQLXML, Time, Timestamp}
import java.time.Instant
import java.util.{Calendar, Date, UUID}
import org.specs2.mock.Mockito
import org.specs2.mutable._
import scala.collection.JavaConverters._

case class TestRecord(
  id: Long,
  name: String
)

object TestRecord{
  implicit val TestRecordRowParser = new RowParser[TestRecord] {
    def parse(row: SqlRow): TestRecord = TestRecord(
      row.long("id"),
      row.string("name")
    )
  }
}

class SqlResultSpec extends Specification with Mockito {
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
    val rs = mock[java.sql.ResultSet]
    (rs, SqlRow(rs), SqlResult(rs))
  }

  implicit val con: Connection = null

  "asSingle" should {
    "return a single row with an explicit parser" in {
      val (rs, _, result) = getMocks

      rs.getRow returns 0 thenReturn 1
      rs.next returns true thenReturns false
      rs.getLong("id") returns (100L: java.lang.Long)
      rs.getString("name") returns "the name"

      result.asSingle(parser) mustEqual TestRecord(100L, "the name")
    }

    "return a single row with an implicit parser" in {
      val (rs, _, result) = getMocks

      rs.getRow returns 0 thenReturn 1
      rs.next returns true thenReturns false
      rs.getLong("id") returns (100L: java.lang.Long)
      rs.getString("name") returns "the name"

      result.asSingle[TestRecord] mustEqual TestRecord(100L, "the name")
    }
  }

  "asSingleOption" should {
    def init(rs: java.sql.ResultSet, next: Boolean) = {
      rs.getRow returns 0 thenReturn 1
      rs.next returns next
      rs.getLong("id") returns (100L: java.lang.Long)
      rs.getString("name") returns "the name"
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

      rs.getRow returns    0 thenReturn    1 thenReturn    2 thenReturn    3
      rs.next   returns true thenReturn true thenReturn true thenReturn false
      rs.getLong("id") returns (100L: java.lang.Long)
      rs.getString("name") returns "the name"

      result.asList(parser) mustEqual List(TestRecord(100L, "the name"), TestRecord(100L, "the name"), TestRecord(100L, "the name"))
    }

    "return an empty list with an explicit parser" in {
      val (rs, _, result) = getMocks

      rs.getRow returns 0
      rs.next returns false

      result.asList(parser) mustEqual List()
    }

    "return a list of 3 elements with an implicit parser" in {
      val (rs, _, result) = getMocks

      rs.getRow returns    0 thenReturn    1 thenReturn    2 thenReturn    3
      rs.next   returns true thenReturn true thenReturn true thenReturn false
      rs.getLong("id") returns (100L: java.lang.Long)
      rs.getString("name") returns "the name"

      result.asList[TestRecord] mustEqual List(TestRecord(100L, "the name"), TestRecord(100L, "the name"), TestRecord(100L, "the name"))
    }

    "return an empty list with an implicit parser" in {
      val (rs, _, result) = getMocks

      rs.getRow returns 0
      rs.next returns false

      result.asList[TestRecord] mustEqual List()
    }
  }

  "asMap" should {
    "return a map of 3 elements with an explicit parser" in {
      val (rs, _, result) = getMocks

      rs.getRow returns    0 thenReturn    1 thenReturn    2 thenReturn    3
      rs.next   returns true thenReturn true thenReturn true thenReturn false
      rs.getLong("id") returns 1L thenReturns 2L thenReturns 3L
      rs.getString("name") returns "the name"

      val res = result.asMap(pairparser)
      res(1L) mustEqual TestRecord(1L, "the name")
      res(2L) mustEqual TestRecord(2L, "the name")
      res(3L) mustEqual TestRecord(3L, "the name")
    }

    "return an empty map with an explicit parser" in {
      val (rs, _, result) = getMocks
      rs.getRow returns 0
      rs.next returns false
      result.asMap(pairparser) mustEqual Map()
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

      rs.getRow returns    0 thenReturn    1 thenReturn    2 thenReturn    3
      rs.next   returns true thenReturn true thenReturn true thenReturn false
      rs.getLong("id") returns (1: L) thenReturns (2: L) thenReturns (3: L)
      rs.getString("name") returns "the name"

      val res = result.asMap[Long, TestRecord]
      res(1L) mustEqual TestRecord(1L, "the name")
      res(2L) mustEqual TestRecord(2L, "the name")
      res(3L) mustEqual TestRecord(3L, "the name")
    }

    "return an empty map with an implicit parser" in {
      val (rs, _, result) = getMocks
      rs.getRow returns 0
      rs.next returns false
      result.asMap[Long, TestRecord] mustEqual Map()
    }
  }

  "asMultiMap" should {
    "return a multimap of 2 keys with 2 entries in each" in {
      val (rs, _, result) = getMocks
      import java.lang.{Long => L}

      rs.getRow returns    0 thenReturn    1 thenReturn    2 thenReturn    3 thenReturn     4
      rs.next   returns true thenReturn true thenReturn true thenReturn true thenReturn false
      rs.getString("id") returns "1" thenReturns "2" thenReturns "1" thenReturns "2"
      rs.getString("name") returns "one" thenReturns "two" thenReturns "three" thenReturns "four"

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

      rs.next returns true
      rs.getObject(1) returns (2: java.lang.Long)

      result.asScalar[Long] must_== 2L
    }

    "ignore other result values" in {
      val (rs, _, result) = getMocks

      rs.next returns true
      rs.getObject(1) returns ("test": java.lang.String)
      rs.getObject(2) returns (2L: java.lang.Long)

      result.asScalar[String] must_== "test"
    }

    "return null if there are no rows" in {
      val (rs, _, result) = getMocks

      rs.next returns false

      result.asScalarOption[Long] must_== None
    }
  }

  "extractOption" should {
    "Extract a Some" in {
      val (rs, row, _) = getMocks

      val name: Object = "hello"
      rs.getObject("name") returns name

      val id: Object = 12: java.lang.Integer
      rs.getObject("id") returns id


      val nameOpt = row.extractOption("name") { any =>
        any match {
          case x: String => x
          case _ => ""
        }
      }

      nameOpt must beSome("hello")

      val idOpt = row.extractOption("id") { any =>
        any match {
          case x: Int => x
          case _ => 0
        }
      }

      idOpt must beSome(12)
    }

    "Extract a None" in {
      val (rs, row, _) = getMocks

      rs.getObject("null") returns null

      val nullOpt = row.extractOption("null") { _ => "hello" }

      nullOpt must beNone
    }
  }

  "getRow" should {
    "return current row number of a ResultSet" in {
      val (rs, row, _) = getMocks

      rs.getRow() returns 3
      row.getRow mustEqual 3
    }
  }

  "wasNull" should {
    "return true if the last read was null" in {
      val (rs, _, result) = getMocks

      rs.wasNull() returns true
      result.wasNull mustEqual true
    }
  }

  "strictArray" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val arr = mock[java.sql.Array]
      rs.getArray("array") returns arr
      row.strictArray("array") mustEqual arr
      row.strictArrayOption("array") must beSome(arr)
    }
  }

  "strictAsciiStream" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val stream = new ByteArrayInputStream("hello".getBytes())
      rs.getAsciiStream("ascii-stream") returns stream
      row.strictAsciiStream("ascii-stream") mustEqual stream
      row.strictAsciiStreamOption("ascii-stream") must beSome(stream)
    }
  }

  "strictBigDecimal" should {
    "properly pass through the call to ResultSet and returns a scala.math.BigDecimal" in {
      val (rs, row, _) = getMocks

      rs.getBigDecimal("big-decimal") returns new java.math.BigDecimal("100.9999")
      row.strictBigDecimal("big-decimal") mustEqual BigDecimal("100.9999")
      row.strictBigDecimalOption("big-decimal") must beSome(BigDecimal("100.9999"))
    }
  }

  "strictBinaryStream" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val stream = new ByteArrayInputStream("hello".getBytes())
      rs.getBinaryStream("binary-stream") returns stream
      row.strictBinaryStream("binary-stream") mustEqual stream
      row.strictBinaryStreamOption("binary-stream") must beSome(stream)
    }
  }

  "strictBlob" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val blob = mock[java.sql.Blob]
      rs.getBlob("blob") returns blob
      row.strictBlob("blob") mustEqual blob
      row.strictBlobOption("blob") must beSome(blob)
    }
  }

  "strictBoolean" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = true
      rs.getBoolean("strictBoolean") returns res
      row.strictBoolean("strictBoolean") mustEqual res
      row.strictBooleanOption("strictBoolean") must beSome(res)
    }
  }

  "strictByte" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res: Byte = 1
      rs.getByte("strictByte") returns res
      row.strictByte("strictByte") mustEqual res
      row.strictByteOption("strictByte") must beSome(res)
    }
  }

  "strictBytes" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res: Array[Byte] = Array(1,2,3)
      rs.getBytes("strictBytes") returns res
      row.strictBytes("strictBytes") mustEqual res
      row.strictBytesOption("strictBytes") must beSome(res)
    }
  }

  "strictCharacterStream" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = mock[Reader]
      rs.getCharacterStream("strictCharacterStream") returns res
      row.strictCharacterStream("strictCharacterStream") mustEqual res
      row.strictCharacterStreamOption("strictCharacterStream") must beSome(res)
    }
  }

  "strictClob" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = mock[Clob]
      rs.getClob("strictClob") returns res
      row.strictClob("strictClob") mustEqual res
      row.strictClobOption("strictClob") must beSome(res)
    }
  }

  "strictDate" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = mock[java.sql.Date]
      rs.getDate("strictDate") returns res
      row.strictDate("strictDate") mustEqual res
      row.strictDateOption("strictDate") must beSome(res)

      val cal = Calendar.getInstance()
      rs.getDate("strictDate", cal) returns res
      row.strictDate("strictDate", cal) mustEqual res
      row.strictDateOption("strictDate", cal) must beSome(res)
    }
  }

  "strictDouble" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res: Double = 1.1
      rs.getDouble("strictDouble") returns res
      row.strictDouble("strictDouble") mustEqual res
      row.strictDoubleOption("strictDouble") must beSome(res)
    }
  }

  "strictFloat" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = 1.1f
      rs.getFloat("strictFloat") returns res
      row.strictFloat("strictFloat") mustEqual res
      row.strictFloatOption("strictFloat") must beSome(res)
    }
  }

  "strictInt" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = 1
      rs.getInt("strictInt") returns res
      row.strictInt("strictInt") mustEqual res
      row.strictIntOption("strictInt") must beSome(res)
    }
  }

  "strictIntOption" should {
    "return None when the value in the database is null" in {
      val (rs, row, _) = getMocks

      val res = 0
      rs.getInt("strictInt") returns res
      rs.wasNull returns true
      row.strictIntOption("strictInt") must beNone
    }

    "return Some when the value in the database is not null" in {
      val (rs, row, _) = getMocks

      val res = 0
      rs.getInt("strictInt") returns res
      rs.wasNull returns false
      row.strictIntOption("strictInt") must beSome(res)
    }
  }

  "strictLong" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = 1000L
      rs.getLong("strictLong") returns res
      row.strictLong("strictLong") mustEqual res
      row.strictLongOption("strictLong") must beSome(res)
    }
  }

  "strictNCharacterStream" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = mock[Reader]
      rs.getNCharacterStream("strictNCharacterStream") returns res
      row.strictNCharacterStream("strictNCharacterStream") mustEqual res
      row.strictNCharacterStreamOption("strictNCharacterStream") must beSome(res)
    }
  }

  "strictNClob" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = mock[NClob]
      rs.getNClob("strictNClob") returns res
      row.strictNClob("strictNClob") mustEqual res
      row.strictNClobOption("strictNClob") must beSome(res)
    }
  }

  "strictNString" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = "hello"
      rs.getNString("strictNString") returns res
      row.strictNString("strictNString") mustEqual res
      row.strictNStringOption("strictNString") must beSome(res)
    }
  }

  "strictObject" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res: Object = 123: java.lang.Integer
      rs.getObject("strictObject") returns res
      row.strictObject("strictObject") mustEqual res
      row.strictObjectOption("strictObject") must beSome(res)

      val map = Map[String,Class[_]]()
      rs.getObject("strictObject", map.asJava) returns res
      row.strictObject("strictObject", map) mustEqual res
      row.strictObjectOption("strictObject", map) must beSome(res)
    }
  }

  "strictRef" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = mock[Ref]
      rs.getRef("strictRef") returns res
      row.strictRef("strictRef") mustEqual res
      row.strictRefOption("strictRef") must beSome(res)
    }
  }

  "strictRowId" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = mock[RowId]
      rs.getRowId("strictRowId") returns res
      row.strictRowId("strictRowId") mustEqual res
      row.strictRowIdOption("strictRowId") must beSome(res)
    }
  }

  "strictShort" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res: Short = 1
      rs.getShort("strictShort") returns res
      row.strictShort("strictShort") mustEqual res
      row.strictShortOption("strictShort") must beSome(res)
    }
  }

  "strictSQLXML" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = mock[SQLXML]
      rs.getSQLXML("strictSQLXML") returns res
      row.strictSQLXML("strictSQLXML") mustEqual res
      row.strictSQLXMLOption("strictSQLXML") must beSome(res)
    }
  }

  "strictString" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = "hello"
      rs.getString("strictString") returns res
      row.strictString("strictString") mustEqual res
      row.strictStringOption("strictString") must beSome(res)
    }
  }

  "strictTime" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = mock[Time]
      rs.getTime("strictTime") returns res
      row.strictTime("strictTime") mustEqual res
      row.strictTimeOption("strictTime") must beSome(res)

      val cal = Calendar.getInstance()
      rs.getTime("strictTime", cal) returns res
      row.strictTime("strictTime", cal) mustEqual res
      row.strictTimeOption("strictTime", cal) must beSome(res)
    }
  }

  "strictTimestamp" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = mock[Timestamp]
      rs.getTimestamp("strictTimestamp") returns res
      row.strictTimestamp("strictTimestamp") mustEqual res
      row.strictTimestampOption("strictTimestamp") must beSome(res)

      val cal = Calendar.getInstance()
      rs.getTimestamp("strictTimestamp", cal) returns res
      row.strictTimestamp("strictTimestamp", cal) mustEqual res
      row.strictTimestampOption("strictTimestamp", cal) must beSome(res)
    }
  }

  "strictURL" should {
    "properly pass through the call to ResultSet" in {
      val (rs, row, _) = getMocks

      val res = new URL("http://localhost")
      rs.getURL("strictURL") returns res
      row.strictURL("strictURL") mustEqual res
      row.strictURLOption("strictURL") must beSome(res)
    }
  }

  "string" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val res = "hello"
      rs.getString("string") returns res
      row.string("string") mustEqual res
      row.stringOption("string") must beSome(res)
    }
  }

  "int" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val res = 10: java.lang.Integer
      rs.getInt("int") returns res
      row.int("int") mustEqual res
      row.intOption("int") must beSome(res)
    }
  }

  "intOption" should {
    "return None if the value in the database is null" in {
      val (rs, row, _) = getMocks

      rs.wasNull returns true
      row.intOption("int") must beNone
    }

    "return Some(0) if the value in the database was really 0" in {
      val (rs, row, _) = getMocks

      val res = 0 : java.lang.Integer
      rs.getInt("int") returns res
      rs.wasNull returns false
      row.intOption("int") must beSome(res)
    }
  }

  "double" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val res = 1.1: java.lang.Double
      rs.getDouble("double") returns res
      row.double("double") mustEqual res
      row.doubleOption("double") must beSome(res)
    }
  }

  "short" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val res: Short = 1
      rs.getShort("short") returns res
      row.short("short") mustEqual res
      row.shortOption("short") must beSome(res)
    }
  }

  "byte" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val res: Byte = 1
      rs.getByte("byte") returns res
      row.byte("byte") mustEqual res
      row.byteOption("byte") must beSome(res)
    }
  }

  "boolean" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val res = true
      rs.getBoolean("boolean") returns res
      row.bool("boolean") mustEqual res
      row.boolOption("boolean") must beSome(res)
    }
  }

  "long" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val res: java.lang.Long = 100000L
      rs.getLong("long") returns res
      row.long("long") mustEqual res
      row.longOption("long") must beSome(res)
    }
  }

  "bigInt" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val number = 1010101

      val int: Object = number.toInt: java.lang.Integer
      rs.getObject("bigInt") returns int
      row.bigInt("bigInt") mustEqual BigInt(number)
      row.bigIntOption("bigInt") must beSome(BigInt(number))

      val long: Object = number.toLong: java.lang.Long
      rs.getObject("bigInt") returns long
      row.bigInt("bigInt") mustEqual BigInt(number)
      row.bigIntOption("bigInt") must beSome(BigInt(number))

      val string: Object = number.toString
      rs.getObject("bigInt") returns string
      row.bigInt("bigInt") mustEqual BigInt(number)
      row.bigIntOption("bigInt") must beSome(BigInt(number))

      val bigint: Object = new java.math.BigInteger(number.toString)
      rs.getObject("bigInt") returns bigint
      row.bigInt("bigInt") mustEqual BigInt(number)
      row.bigIntOption("bigInt") must beSome(BigInt(number))
    }
  }

  "bigDecimal" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val number = 1.013

      val bigint = new java.math.BigDecimal(number.toString)
      rs.getBigDecimal("bigDecimal") returns bigint
      row.bigDecimal("bigDecimal") mustEqual BigDecimal(number)
      row.bigDecimalOption("bigDecimal") must beSome(BigDecimal(number))
    }
  }

  "javaBigInteger" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val number = 1010101
      val bigNumber = java.math.BigInteger.valueOf(number)

      val int: Object = number.toInt: java.lang.Integer
      rs.getObject("javaBigInteger") returns int
      row.javaBigInteger("javaBigInteger") mustEqual bigNumber
      row.javaBigIntegerOption("javaBigInteger") must beSome(bigNumber)

      val long: Object = number.toLong: java.lang.Long
      rs.getObject("javaBigInteger") returns long
      row.javaBigInteger("javaBigInteger") mustEqual bigNumber
      row.javaBigIntegerOption("javaBigInteger") must beSome(bigNumber)

      val bigint: Object = new java.math.BigInteger(number.toString)
      rs.getObject("javaBigInteger") returns bigint
      row.javaBigInteger("javaBigInteger") mustEqual bigNumber
      row.javaBigIntegerOption("javaBigInteger") must beSome(bigNumber)

      val str: Object = number.toString
      rs.getObject("javaBigInteger") returns str
      row.javaBigInteger("javaBigInteger") mustEqual bigNumber
      row.javaBigIntegerOption("javaBigInteger") must beSome(bigNumber)
    }
  }

  "javaBigDecimal" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val number = 1

      val bigdec = java.math.BigDecimal.valueOf(number)
      rs.getBigDecimal("javaBigDecimal") returns bigdec
      row.javaBigDecimal("javaBigDecimal") mustEqual new java.math.BigDecimal(number.toString)
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
      timestamp mustNotEqual date
      date mustEqual timestamp

      rs.getTimestamp("date") returns timestamp
      row.date("date") mustEqual timestamp
      row.date("date") mustEqual date
      row.dateOption("date") must beSome(timestamp)
    }
  }

  "instant" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val res = Instant.EPOCH.plusSeconds(10000)
      val timestamp = mock[Timestamp]
      timestamp.toInstant() returns res

      rs.getTimestamp("instant") returns timestamp
      row.instant("instant") mustEqual res
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

      val res = Array[Byte]('1','2','3')
      rs.getObject("byteArray") returns res
      row.byteArray("byteArray") mustEqual res
      row.byteArrayOption("byteArray") must beSome(res)

      val blob = mock[Blob]
      blob.length returns res.length
      blob.getBytes(0, res.length) returns res
      rs.getObject("byteArray") returns blob
      row.byteArray("byteArray") mustEqual res
      row.byteArrayOption("byteArray") must beSome(res)

      val clob = mock[Clob]
      clob.length returns res.length
      clob.getSubString(1, res.length) returns "123"
      rs.getObject("byteArray") returns clob
      row.byteArray("byteArray") mustEqual res
      row.byteArrayOption("byteArray") must beSome(res)
    }
  }

  "uuid" should {
    "return the correct value when stored as a byte array" in {
      val (rs, row, _) = getMocks

      val res = Array[Byte]('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
      rs.getObject("uuid") returns res
      row.uuid("uuid") mustEqual new UUID(3472611983179986487L, 4051376414998685030L)
      row.uuidOption("uuid") must beSome(new UUID(3472611983179986487L, 4051376414998685030L))
    }

    "return the correct value when stored as UUID" in {
      val (rs, row, _) = getMocks

      val res = new UUID(3472611983179986487L, 4051376414998685030L)
      rs.getObject("uuid") returns res
      row.uuid("uuid") mustEqual res
      row.uuidOption("uuid") must beSome(res)
    }
  }

  "uuidFromString" should {
    "return the correct value" in {
      val (rs, row, _) = getMocks

      val res = "000102030405060708090a0b0c0d0e0f"
      rs.getString("uuidFromString") returns res
      row.uuidFromString("uuidFromString") mustEqual new UUID(283686952306183L, 579005069656919567L)
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

      rs.getInt("enum") returns 1 thenReturns 2 thenReturns 3 thenReturns 4
      row.enum("enum", Things) mustEqual Things.one
      row.enum("enum", Things) mustEqual Things.two
      row.enumOption("enum", Things) must beSome(Things.three)
      row.enumOption("enum", Things) must beNone
    }
  }
}
