package com.lucidchart.open.relate.test

import com.lucidchart.open.relate._
import org.specs2.mutable._
import org.specs2.mock.Mockito
import org.mockito.Matchers._
import java.sql.Connection

class SQLSpec extends Specification  with Mockito {
  val connection = mock[Connection]
  connection.prepareStatement(any[String]) returns null

  "The SQL method" should {
    
    "produce 0 parameters for a SQL statement with no parameters" in {
      val sql = SQL("SELECT count(1) FROM table")(connection)
      sql.args must have size(0)
    }

    "produce 1 parameter for a SQL statement with 1 parameter" in {
      val sql = SQL("SELECT * FROM table WHERE param={name}")(connection)
      sql.args must have size(1)
    }

    "produce 3 parameters for a SQL statement with 3 parameters" in {
      val sql = SQL("INSERT INTO table (param1, param2, param3) VALUES ({name1}, {name2}, {name3})")(connection)
      sql.args must have size(3)
    }

    "get the correct parameter names and in order" in {
      val sql = SQL("INSERT INTO table (param1, param2, param3) VALUES ({name1}, {name2}, {name3})")(connection)
      sql.args must_== List("name1", "name2", "name3")
    }

    "have correct number of ?s in replaced query" in {
      val sql = SQL("INSERT INTO table (param1, param2, param3) VALUES ({name1}, {name2}, {name3})")(connection)
      val numQuestionMarks = sql.query.count(_ == '?')
      numQuestionMarks must_== 3
    }

    "strip out all original parameters" in {
      val sql = SQL("INSERT INTO table (param1, param2, param3) VALUES ({name1}, {name2}, {name3})")(connection)
      val originalsRemoved = !((sql.query contains '{') || (sql.query contains '}'))
      originalsRemoved must beTrue
    }
  }

}