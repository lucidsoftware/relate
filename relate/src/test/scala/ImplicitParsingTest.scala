package com.lucidchart.relate

import java.sql.Connection
import org.specs2.mock.Mockito
import org.specs2.mutable._

class ImplicitParsingTest extends Specification with Mockito {
  def getMocks = {
    val rs = mock[java.sql.ResultSet]
    (rs, SqlResult(rs))
  }

  implicit val con: Connection = null

  case class TestRecord(name: String)

  object TestRecord {
    implicit val praser = new RowParser[TestRecord] {
      def parse(result: SqlRow): TestRecord = {
        TestRecord(result.string("name"))
      }
    }
  }

  case class TestKey(key: String)

  object TestKey {
    implicit val parse = new RowParser[TestKey] {
      def parse(result: SqlRow): TestKey = {
        TestKey(result.string("key"))
      }
    }
  }

  "RowParser" should {
    "build a list" in {
      val (rs, result) = getMocks

      rs.getRow returns 0 thenReturns 1 thenReturns 2
      rs.next returns true thenReturns true thenReturns false
      rs.getString("name") returns "hello" thenReturns "world"

      result.as[List[TestRecord]] mustEqual List(
        TestRecord("hello"),
        TestRecord("world")
      )

      success
    }

    "build a seq" in {
      val (rs, result) = getMocks

      rs.getRow returns 0 thenReturns 1 thenReturns 2
      rs.next returns true thenReturns true thenReturns false
      rs.getString("name") returns "hello" thenReturns "world"

      result.as[Seq[TestRecord]] mustEqual Seq(
        TestRecord("hello"),
        TestRecord("world")
      )

      success
    }

    "build an iterable" in {
      val (rs, result) = getMocks

      rs.getRow returns 0 thenReturns 1 thenReturns 2
      rs.next returns true thenReturns true thenReturns false
      rs.getString("name") returns "hello" thenReturns "world"

      result.as[Iterable[TestRecord]] mustEqual Iterable(
        TestRecord("hello"),
        TestRecord("world")
      )

      success
    }

    "build an iterable" in {
      val (rs, result) = getMocks

      rs.getRow returns 0 thenReturns 1 thenReturns 2
      rs.next returns true thenReturns true thenReturns false
      rs.getString("name") returns "hello" thenReturns "world"

      result.as[Iterable[TestRecord]] mustEqual Iterable(
        TestRecord("hello"),
        TestRecord("world")
      )

      success
    }

    "build a map" in {
      val (rs, result) = getMocks

      rs.getRow returns 0 thenReturns 1 thenReturns 2
      rs.next returns true thenReturns true thenReturns false
      rs.getString("name") returns "hello" thenReturns "world"
      rs.getString("key") returns "1" thenReturns "2"

      result.as[Map[TestKey, TestRecord]] mustEqual Map(
        TestKey("1") -> TestRecord("hello"),
        TestKey("2") -> TestRecord("world")
      )
    }

    "build a multi-map" in {
      val (rs, result) = getMocks

      rs.getRow returns 0 thenReturns 1 thenReturns 2 thenReturns 3
      rs.next returns true thenReturns true thenReturns true thenReturns false
      rs.getString("name") returns "hello" thenReturns "world" thenReturns "relate"
      rs.getString("key") returns "1" thenReturns "2" thenReturns "1"

      result.as[Map[TestKey, Set[TestRecord]]] mustEqual Map(
        TestKey("1") -> Set(TestRecord("hello"), TestRecord("relate")),
        TestKey("2") -> Set(TestRecord("world"))
      )
    }

    "build an option of something" in {
      val (rs, result) = getMocks

      rs.getRow returns 0 thenReturns 1 thenReturns 2 thenReturns 3
      rs.next returns true thenReturns true thenReturns true thenReturns false
      rs.getString("name") returns "hello" thenReturns "world" thenReturns "relate"

      result.as[Option[TestRecord]] mustEqual Some(TestRecord("hello"))
    }

    "build a None of something" in {
      val (rs, result) = getMocks

      rs.next returns false

      result.as[Option[TestRecord]] mustEqual None
    }
  }
}
