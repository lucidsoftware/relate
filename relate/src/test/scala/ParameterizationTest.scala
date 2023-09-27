package com.lucidchart.relate

import java.sql.PreparedStatement
import org.specs2.mutable._

class ParameterizationTest extends Specification {
  "parameter conversions" should {

    "convert Array[Byte] into a single parameter" in {
      val byteArrayParam: Parameter = Array[Byte](5, 20, 34, 89, 110)
      val querySql = sql"INSERT INTO myTable (foo) VALUES ($byteArrayParam)"
      querySql.toString mustEqual "INSERT INTO myTable (foo) VALUES (?)"
    }

    "convert Array[Long] into a tuple" in {
      val longArrayParam: Parameter = Array[Long](1L, 5L, 7L)
      val querySql = sql"INSERT INTO myTable (foo) VALUES ($longArrayParam)"
      querySql.toString mustEqual "INSERT INTO myTable (foo) VALUES (?,?,?)"
    }
  }

  "tuple paramater" should {
    "use sub-parameter placeholders" in {
      class CustomParameter(value: Int) extends SingleParameter {
        protected[this] def set(statement: PreparedStatement, i: Int) = implicitly[Parameterizable[Int]].set(statement, i, value)
        override def appendPlaceholders(stringBuilder: StringBuilder) = stringBuilder.append("?::smallint")
      }
      val querySql = sql"INSERT INTO myTable (foo, bar) VALUES (${(1, new CustomParameter(1))})"
      querySql.toString mustEqual("INSERT INTO myTable (foo, bar) VALUES (?,?::smallint)")
    }
  }
}
