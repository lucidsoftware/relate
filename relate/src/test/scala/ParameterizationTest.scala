package com.lucidchart.relate

import java.sql.PreparedStatement
import org.specs2.mutable._
import scala.jdk.CollectionConverters._

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

    "interpolate HashSets properly" in {
      // note that HashSets don't iterate consistently: zipWithIndex and head get different "first" elements
      // (also that zipWithIndex on a HashSet returns another HashSet)
      val hashSet: Set[Int] = scala.collection.immutable.HashSet(1, 2, 3)
      hashSet.zipWithIndex.head._2 mustNotEqual 0
      // even so, we should interpolate it correctly
      val querySql = sql"SELECT * FROM myTable WHERE id IN ($hashSet)"
      querySql.toString mustEqual "SELECT * FROM myTable WHERE id IN (?,?,?)"
    }
  }

  "tuple paramater" should {
    "use sub-parameter placeholders" in {
      class CustomParameter(value: Int) extends SingleParameter {
        protected[this] def set(statement: PreparedStatement, i: Int) =
          implicitly[Parameterizable[Int]].set(statement, i, value)
        override def appendPlaceholders(stringBuilder: StringBuilder) = stringBuilder.append("?::smallint")
      }
      val querySql = sql"INSERT INTO myTable (foo, bar) VALUES (${(1, new CustomParameter(1))})"
      querySql.toString mustEqual "INSERT INTO myTable (foo, bar) VALUES (?,?::smallint)"
    }
  }
}
