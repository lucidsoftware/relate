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
      val hashSet: Set[Int] = scala.collection.immutable.HashSet(1, 2, 3)
      val querySql = sql"SELECT * FROM myTable WHERE id IN ($hashSet)"
      querySql.toString mustEqual "SELECT * FROM myTable WHERE id IN (?,?,?)"
    }

    "provide placeholders and parameters in the same order" in {
      val setParams = scala.collection.mutable.Map.empty[Int, Int]
      case class Param(int: Int) extends SingleParameter {
        protected[this] def set(statement: PreparedStatement, i: Int) = setParams(i) = int
        override def placeholder = int.toString
      }

      val paramsSet: Parameter = Set(Param(1), Param(2), Param(3))
      paramsSet.parameterize(null, 1)
      val query = sql"SELECT * FROM myTable WHERE id IN ($paramsSet)".toString

      setParams must haveSize(3)
      val order = setParams.toSeq.sortBy(_._1).map(_._2).mkString(",")
      // the order of the "placeholders" should match the order of the parameters
      query mustEqual s"SELECT * FROM myTable WHERE id IN ($order)"
    }
  }

  "tuple paramater" should {
    "use sub-parameter placeholders" in {
      class CustomParameter(value: Int) extends SingleParameter {
        protected[this] def set(statement: PreparedStatement, i: Int) =
          implicitly[Parameterizable[Int]].set(statement, i, value)
        override def placeholder = "?::smallint"
      }
      val querySql = sql"INSERT INTO myTable (foo, bar) VALUES (${(1, new CustomParameter(1))})"
      querySql.toString mustEqual "INSERT INTO myTable (foo, bar) VALUES (?,?::smallint)"
    }
  }
}
