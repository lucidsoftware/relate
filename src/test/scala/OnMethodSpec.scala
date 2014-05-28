package com.lucidchart.open.relate.test

import com.lucidchart.open.relate._
import com.lucidchart.open.relate.Query._
import java.sql.{Connection, PreparedStatement}
import java.util.Date
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

      SQL(sql.format("{param}")).on { implicit statement =>
        int("param", 10)
      }.executeQuery()(connection)

      there was one(stmt).setInt(1, 10)
    }


    "work with chained 'on' method calls" in {
      val sql = "SELECT * FROM another WHERE one=%s AND two=%s"
      val (connection, stmt) = getMocks
      connection.prepareStatement(sql.format("?", "?")) returns stmt

      SQL(sql.format("{param}", "{name}")).on { implicit statement =>
        string("param", "string")
      }.on { implicit statement =>
        double("name", 20.1)
      }.executeQuery()(connection)

      there was one(stmt).setString(1, "string") andThen one(stmt).setDouble(2, 20.1)
    }

    "work with out of order params" in {
      val sql = "SELECT * FROM table WHERE one=%s AND two=%s"
      val (connection, stmt) = getMocks
      connection.prepareStatement(sql.format("?", "?")) returns stmt

      SQL(sql.format("{one}", "{two}")).on { implicit statement =>
        float("two", 1.5f)
        string("one", "test")
      }.executeQuery()(connection)

      there was one(stmt).setFloat(2, 1.5f) andThen one(stmt).setString(1, "test")
    }

    "work for select" in {
      val sql = "SELECT * FROM table WHERE something=%s"
      val (connection, stmt) = getMocks
      connection.prepareStatement(sql.format("?")) returns stmt

      SQL(sql.format("{param}")).on { implicit statement =>
        int("param", 10)
      }.executeQuery()(connection)

      there was one(stmt).setInt(1, 10)
    }

    "work for insert" in {
      val sql = """INSERT INTO table (one, two, date, optDate) VALUES (%s, %s, %s, %s)"""
      val (connection, stmt) = getMocks
      connection.prepareStatement(sql.format("?", "?", "?", "?")) returns stmt

      val d = new Date
      SQL(sql.format("{one}", "{two}", "{date}", "{optDate}")).on { implicit statement =>
        int("one", 5)
        int("two", 6)
        date("date", d)
        dateOption("optDate", Some(d))
      }.executeUpdate()(connection)

      there was one(stmt).setInt(1, 5) andThen one(stmt).setInt(2, 6) andThen one(stmt).setTimestamp(3, new java.sql.Timestamp(d.getTime)) andThen
        one(stmt).setTimestamp(4, new java.sql.Timestamp(d.getTime))
    }

    "work for update" in {
      val sql = "UPDATE table SET column=%s"
      val (connection, stmt) = getMocks
      connection.prepareStatement(sql.format("?")) returns stmt

      SQL(sql.format("{first}")).on { implicit statement =>
        int("first", 1)
      }.executeUpdate()(connection)

      there was one(stmt).setInt(1, 1)
    }

    "work for delete" in {
      val sql = "DELETE FROM table WHERE column=%s"
      val (connection, stmt) = getMocks
      connection.prepareStatement(sql.format("?")) returns stmt

      SQL(sql.format("{one}")).on { implicit statement =>
        int("one", 2)
      }.executeUpdate()(connection)

      there was one(stmt).setInt(1, 2)
    }

    "work for multiple instances of same parameter name" in {
      val sql = "SELECT * FROM table WHERE column1=%s AND column2=%s AND column3=%s"
      val (connection, stmt) = getMocks
      connection.prepareStatement(sql.format("?", "?", "?")) returns stmt

      SQL(sql.format("{same}", "{another}", "{same}")).on { implicit statement =>
        int("same", 2)
        string("another", "value")
      }.executeUpdate()(connection)

      there was one(stmt).setInt(1, 2) andThen one(stmt).setInt(3, 2) andThen 
        one(stmt).setString(2, "value")
    }

    "work for list" in {
      val sqlOriginal = "SELECT * FROM table WHERE id IN ({ids})"
      val sql = "SELECT * FROM table WHERE id IN (?,?,?)"
      val (connection, stmt) = getMocks
      connection.prepareStatement(sql) returns stmt

      SQL(sqlOriginal).expand { implicit query =>
        commaSeparated("ids", 3)
      }.on { implicit statement =>
        int("ids", List(1, 2, 3))
      }.executeUpdate()(connection)

      there was one(stmt).setInt(1, 1) andThen one(stmt).setInt(2, 2) andThen one(stmt).setInt(3, 3)
    }

    "work for multiple lists" in {
      val sqlOriginal = "SELECT * FROM table WHERE id IN ({ids}) AND value IN ({values})"
      val sql = "SELECT * FROM table WHERE id IN (?,?,?) AND value IN (?,?)"
      val (connection, stmt) = getMocks
      connection.prepareStatement(sql) returns stmt

      SQL(sqlOriginal).expand { implicit query =>
        commaSeparated("ids", 3)
        commaSeparated("values", 2)
      }.on { implicit statement =>
        string("values", List("one", "two"))
        int("ids", List(1, 2, 3))
      }.executeUpdate()(connection)

      there was one(stmt).setString(4, "one") andThen one(stmt).setString(5, "two") andThen
        one(stmt).setInt(1, 1) andThen one(stmt).setInt(2, 2) andThen one(stmt).setInt(3, 3)
    }

    "work for tuple list" in {
      val sqlOriginal = "INSERT INTO table (one, two, three) VALUES {tuples}"
      val sql = "INSERT INTO table (one, two, three) VALUES (?,?,?),(?,?,?)"
      val (connection, stmt) = getMocks
      connection.prepareStatement(sql) returns stmt

      val records = List(
        (2, "string", 1.5),
        (3, "value", .75)
      )

      SQL(sqlOriginal).expand { implicit query =>
        tupled("tuples", List("one", "two", "three"), records.size)
      }.on { implicit statement =>
        tuples("tuples", records) { case (tuple, tupleStmt) =>
          tupleStmt.string("two", tuple._2)
          tupleStmt.int("one", tuple._1)
          tupleStmt.double("three", tuple._3)
        }
      }.executeUpdate()(connection)

      there was one(stmt).setInt(1, 2) andThen one(stmt).setString(2, "string") andThen
        one(stmt).setDouble(3, 1.5) andThen one(stmt).setInt(4, 3) andThen 
        one(stmt).setString(5, "value") andThen one(stmt).setDouble(6, .75)
    }
  }

}
