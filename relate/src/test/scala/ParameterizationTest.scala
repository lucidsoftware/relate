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

    "convert Array[Array[Byte]] into a tuple of single parameter" in {
      val byteArrayListParam: Parameter = Array[Array[Byte]](Array[Byte](5, 7), Array[Byte](11, 13))
      val querySql = sql"INSERT INTO myTable (foo) VALUES ($byteArrayListParam)"
      querySql.toString mustEqual "INSERT INTO myTable (foo) VALUES (?,?)"
    }

    "convert List[Array[Byte]] into a tuple of single parameter" in {
      val byteArrayListParam: Parameter = List[Array[Byte]](Array[Byte](5, 7), Array[Byte](11, 13))
      val querySql = sql"INSERT INTO myTable (foo) VALUES ($byteArrayListParam)"
      querySql.toString mustEqual "INSERT INTO myTable (foo) VALUES (?,?)"
    }

  }
}
