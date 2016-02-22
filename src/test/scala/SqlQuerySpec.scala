package com.lucidchart.open.relate

import java.sql.Connection
import java.sql.PreparedStatement
import org.specs2.mutable._
import org.specs2.mock.Mockito

class SqlQuerySpec extends Specification with Mockito {

  "ExpandableQuery.withTimeout" should {
    class TestEq extends ExpandableQuery("")

    "set the timeout" in {
      val eq = new TestEq().withTimeout(10)
      eq.timeout must beSome(10)
    }

    "not set the timeout" in {
      val eq = new TestEq()
      eq.timeout must beNone
    }
  }
  
}