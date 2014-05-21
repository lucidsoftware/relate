package com.lucidchart.open.relate.test

import com.lucidchart.open.relate._
import com.lucidchart.open.relate.SqlTypes._
import java.sql.{Connection, PreparedStatement}
import org.mockito.Matchers._
import org.specs2.mutable._
import org.specs2.mock.Mockito

class OnMethodSpec extends Specification with Mockito {
  
  def getMocks = (mock[Connection], mock[PreparedStatement])

  "The on method" should {

    "work with one param" in  {
      val sql = "SELECT * FROM table WHERE param=%s"
      val (connection, stmt) = getMocks
      connection.prepareStatement(sql.format("?")) returns stmt

      SQL(sql.format("{param}"))(connection).on { implicit statement =>
        int("param", 10)
      }

      there was one(stmt).setInt(1, 10)
    }


    "work with chained 'on' method calls" in {
      val sql = "SELECT * FROM another WHERE one=%s AND two=%s"
      val (connection, stmt) = getMocks
      connection.prepareStatement(sql.format("?", "?")) returns stmt

      SQL(sql.format("{param}", "{name}"))(connection).on { implicit statement =>
        string("param", "string")
      }.on { implicit statement =>
        double("name", 20.1)
      }

      there was one(stmt).setString(1, "string") andThen one(stmt).setDouble(2, 20.1)
    }

    "work with out of order params" in {
      val sql = "SELECT * FROM table WHERE one=%s AND two=%s"
      val (connection, stmt) = getMocks
      connection.prepareStatement(sql.format("?", "?")) returns stmt

      SQL(sql.format("{one}", "{two}"))(connection).on { implicit statement =>
        float("two", 1.5f)
        string("one", "test")
      }

      there was one(stmt).setFloat(2, 1.5f) andThen one(stmt).setString(1, "test")
    }

    "work for select" in {
      val sql = "SELECT * FROM table WHERE something=%s"
      val (connection, stmt) = getMocks
      connection.prepareStatement(sql.format("?")) returns stmt

      SQL(sql.format("{param}"))(connection).on { implicit statement =>
        int("param", 10)
      }

      there was one(stmt).setInt(1, 10)
    }

    "work for insert" in {
      val sql = """INSERT INTO table (one, two) VALUES (%s, %s)"""
      val (connection, stmt) = getMocks
      connection.prepareStatement(sql.format("?", "?")) returns stmt

      SQL(sql.format("{one}", "{two}"))(connection).on { implicit statement =>
        int("one", 5)
        int("two", 6)
      }

      there was one(stmt).setInt(1, 5) andThen one(stmt).setInt(2, 6)
    }

    "work for update" in {
      val sql = "UPDATE table SET column=%s"
      val (connection, stmt) = getMocks
      connection.prepareStatement(sql.format("?")) returns stmt

      SQL(sql.format("{first}"))(connection).on { implicit statement =>
        int("first", 1)
      }

      there was one(stmt).setInt(1, 1)
    }

    "work for delete" in {
      val sql = "DELETE FROM table WHERE column=%s"
      val (connection, stmt) = getMocks
      connection.prepareStatement(sql.format("?")) returns stmt

      SQL(sql.format("{one}"))(connection) on { implicit statement =>
        int("one", 2)
      }

      there was one(stmt).setInt(1, 2)
    }

    //work with all datatypes

    //work with mixed datatypes

  }

}
