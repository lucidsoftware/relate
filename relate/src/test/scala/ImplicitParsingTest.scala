package com.lucidchart.relate

import java.sql.Connection
import org.mockito.Mockito._
import org.specs2.mutable._

class ImplicitParsingTest extends Specification {
  def getMocks = {
    val rs = mock(classOf[java.sql.ResultSet])
    (rs, SqlResult(rs))
  }

  implicit val con: Connection = null

  case class TestRecord(name: String)

  object TestRecord {
    implicit val praser: RowParser[TestRecord] = new RowParser[TestRecord] {
      def parse(result: SqlRow): TestRecord = {
        TestRecord(result.string("name"))
      }
    }
  }

  case class TestKey(key: String)

  object TestKey {
    implicit val parse: RowParser[TestKey] = new RowParser[TestKey] {
      def parse(result: SqlRow): TestKey = {
        TestKey(result.string("key"))
      }
    }
  }

  /**
   * Set up default mocking for a ResultSet
   */
  def mockResultSet(rs: java.sql.ResultSet): Unit = {
      when(rs.getRow).thenReturn(0).thenReturn(1).thenReturn(2)
      when(rs.next).thenReturn(true).thenReturn(true).thenReturn(false)
      when(rs.getString("name")).thenReturn("hello").thenReturn("world")
  }

  "RowParser" should {
    "build a list" in {
      val (rs, result) = getMocks

      mockResultSet(rs)

      result.as[List[TestRecord]] must beEqualTo(List(
        TestRecord("hello"),
        TestRecord("world")
      ))
    }

    "build a seq" in {
      val (rs, result) = getMocks

      mockResultSet(rs)

      result.as[Seq[TestRecord]] must beEqualTo(Seq(
        TestRecord("hello"),
        TestRecord("world")
      ))
    }

    "build an iterable" in {
      val (rs, result) = getMocks

      mockResultSet(rs)

      result.as[Iterable[TestRecord]] must beEqualTo(Iterable(
        TestRecord("hello"),
        TestRecord("world")
      ))
    }

    "build an iterable" in {
      val (rs, result) = getMocks

      mockResultSet(rs)

      result.as[Iterable[TestRecord]] must beEqualTo(Iterable(
        TestRecord("hello"),
        TestRecord("world")
      ))
    }

    "build a map" in {
      val (rs, result) = getMocks

      mockResultSet(rs)
      when(rs.getString("key")) thenReturn "1" thenReturn "2"

      result.as[Map[TestKey, TestRecord]] must beEqualTo(Map(
        TestKey("1") -> TestRecord("hello"),
        TestKey("2") -> TestRecord("world")
      ))
    }

    "build a multi-map" in {
      val (rs, result) = getMocks

      when(rs.getRow) thenReturn 0 thenReturn 1 thenReturn 2 thenReturn 3
      when(rs.next) thenReturn true thenReturn true thenReturn true thenReturn false
      when(rs.getString("name")) thenReturn "hello" thenReturn "world" thenReturn "relate"
      when(rs.getString("key")) thenReturn "1" thenReturn "2" thenReturn "1"

      result.as[Map[TestKey, Set[TestRecord]]] must beEqualTo(Map(
        TestKey("1") -> Set(TestRecord("hello"), TestRecord("relate")),
        TestKey("2") -> Set(TestRecord("world"))
      ))
    }

    "build an option of something" in {
      val (rs, result) = getMocks

      when(rs.getRow) thenReturn 0 thenReturn 1 thenReturn 2 thenReturn 3
      when(rs.next) thenReturn true thenReturn true thenReturn true thenReturn false
      when(rs.getString("name")) thenReturn "hello" thenReturn "world" thenReturn "relate"

      result.as[Option[TestRecord]] must beSome(TestRecord("hello"))
    }

    "build a None of something" in {
      val (rs, result) = getMocks

      when(rs.next) thenReturn false

      result.as[Option[TestRecord]] must beNone
    }
  }
}
