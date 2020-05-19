package com.lucidchart.relate

import org.specs2.mutable._

class ParameterizationTest extends Specification {
  "parameter conversions" should {

    "convert Array[Byte] into a single parameter" in {
      val byteArrayParam: Parameter = Array[Byte](5, 20, 34, 89, 110)
      val querySql = sql"INSERT INTO myTable (foo) VALUES ($byteArrayParam)"
      querySql.toString mustEqual("INSERT INTO myTable (foo) VALUES (?)")
    }

    "convert Array[Long] into a tuple" in {
      val longArrayParam: Parameter = Array[Long](1l, 5l, 7l)
      val querySql = sql"INSERT INTO myTable (foo) VALUES ($longArrayParam)"
      querySql.toString mustEqual("INSERT INTO myTable (foo) VALUES (?,?,?)")
    }
  }
}
