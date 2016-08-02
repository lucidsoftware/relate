package com.lucidchart.open.relate.macros

import com.lucidchart.open.relate._
import com.lucidchart.open.relate.interp._
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import shapeless.test.illTyped

case class Thing(firstName: String, b: Option[Int])
object Thing {
  def apply(a: String): Thing = new Thing(a, None)
}
case class User(firstName: String, lastName: String)

class ParseableTest extends Specification with Mockito {
  "Parseable def macros" should {
    "generate parser" in {
      val rs = mock[java.sql.ResultSet]
      rs.getString("firstName") returns "hi"
      rs.getInt("b") returns 20
      val row = SqlResult(rs)

      val p = generateParseable[Thing]

      p.parse(row) mustEqual(Thing("hi", Some(20)))
    }

    "generate parser w/snake_case columns" in {
      val rs = mock[java.sql.ResultSet]
      rs.getString("first_name") returns "gregg"
      rs.getInt("b") returns 20
      val row = SqlResult(rs)

      val p = generateSnakeParseable[Thing]

      p.parse(row) mustEqual(Thing("gregg", Some(20)))
    }

    "remap column names" in {
      val p = generateParseable[User](Map(
        "firstName" -> "fname",
        "lastName" -> "lname"
      ))

      val rs = mock[java.sql.ResultSet]
      rs.getString("fname") returns "gregg"
      rs.getString("lname") returns "hernandez"
      val row = SqlResult(rs)

      p.parse(row) mustEqual User("gregg", "hernandez")
    }

    "remap column names w/normal tuple syntax" in {
      val p = generateParseable[User](Map(
        ("firstName", "fname"),
        ("lastName", "lname")
      ))

      val rs = mock[java.sql.ResultSet]
      rs.getString("fname") returns "gregg"
      rs.getString("lname") returns "hernandez"
      val row = SqlResult(rs)

      p.parse(row) mustEqual User("gregg", "hernandez")
    }

    "remap some column names" in {
      val p = generateParseable[User](Map(
        "firstName" -> "fname"
      ))

      val rs = mock[java.sql.ResultSet]
      rs.getString("fname") returns "gregg"
      rs.getString("lastName") returns "hernandez"
      val row = SqlResult(rs)

      p.parse(row) mustEqual User("gregg", "hernandez")
    }

    "fail to compile with non-literals" in {
      illTyped(
        """val name = "newName"; generateParseable[User](Map("firstName" -> name))""",
        "Remappings must be literal strings"
      )

      illTyped(
        """val col = "col"; generateParseable[User](Map(col -> "name"))""",
        "Column names must be literal strings"
      )

      success
    }

    "fail to compile with invalid columns" in {
      illTyped(
        """generateParseable[User](Map("badCol" -> "name"))""",
        "badCol is not a member of com.lucidchart.open.relate.macros.User"
      )

      success
    }
  }

  "@Record" should {
    @Record()
    case class SimpleRecord(firstName: String, lastName: Option[String])

    "generate parser" in {
      val rs = mock[java.sql.ResultSet]
      rs.getString("firstName") returns "gregg"
      rs.getString("lastName") returns "hernandez"
      val row = SqlResult(rs)

      implicitly[Parseable[SimpleRecord]].parse(row) mustEqual SimpleRecord("gregg", Some("hernandez"))
    }

    @Record("snakeCase" -> true)
    case class SnakeRecord(firstName: String, lastName: Option[String])

    object SnakeRecord {
      def f(): String = "hello"
    }

    "generate parser w/snake_case columns" in {
      val rs = mock[java.sql.ResultSet]
      rs.getString("first_name") returns "gregg"
      rs.getString("last_name") returns "hernandez"
      val row = SqlResult(rs)

      // verify that this still compiles
      SnakeRecord.f()

      implicitly[Parseable[SnakeRecord]].parse(row) mustEqual SnakeRecord("gregg", Some("hernandez"))
    }

    @Record("colMapping" -> Map("firstName" -> "fname", "lastName" -> "lname"))
    case class RemapRecord(firstName: String, lastName: String)

    "remap column names" in {
      val rs = mock[java.sql.ResultSet]
      rs.getString("fname") returns "gregg"
      rs.getString("lname") returns "hernandez"
      val row = SqlResult(rs)

      implicitly[Parseable[RemapRecord]].parse(row) mustEqual RemapRecord("gregg", "hernandez")
    }

    @Record("colMapping" -> Map(("firstName", "fname"), ("lastName", "lname")))
    case class RemapTRecord(firstName: String, lastName: String)

    "remap column names w/normal tuple syntax" in {
      val rs = mock[java.sql.ResultSet]
      rs.getString("fname") returns "gregg"
      rs.getString("lname") returns "hernandez"
      val row = SqlResult(rs)

      implicitly[Parseable[RemapTRecord]].parse(row) mustEqual RemapTRecord("gregg", "hernandez")
    }

    @Record("colMapping" -> Map("firstName" -> "fname"))
    case class RemapSomeRecord(firstName: String, lastName: String)

    "remap some column names" in {
      val rs = mock[java.sql.ResultSet]
      rs.getString("fname") returns "gregg"
      rs.getString("lastName") returns "hernandez"
      val row = SqlResult(rs)

      implicitly[Parseable[RemapSomeRecord]].parse(row) mustEqual RemapSomeRecord("gregg", "hernandez")
    }

    "fail to compile for anything besides a case class" in {
      illTyped("@Record() class Thing", "@Record must be used on a case class")
      illTyped("@Record() def f()", "@Record must be used on a case class")
      illTyped("@Record() object Thing {}", "@Record must be used on a case class")
      illTyped("@Record() trait Thing", "@Record must be used on a case class")

      success
    }

    "fail to compile when passing in colMapping and snakeCase" in {
      illTyped(
        """@Record("snakeCase" -> true, "colMapping" -> Map[String, String]()) case class Thing(a: String)""",
        "Only one of snakeCase or colMapping can be supplied"
      )

      success
    }

    "fail to compile with non-literals" in {
      illTyped(
        """val sc = true; @Record("snakeCase" -> sc) case class Thing(a: String)""",
        "snakeCase requires a literal true or false value"
      )

      illTyped(
        """val col = "col"; @Record("colMapping" -> Map(col -> "thing")) case class Thing(col: String)""",
        "Column names must be literal strings"
      )

      illTyped(
        """val name = "name"; @Record("colMapping" -> Map("col" -> name)) case class Thing(col: String)""",
        "Remappings must be literal strings"
      )

      success
    }

    "fail to compile with invalid columns" in {
      illTyped(
        """@Record("colMapping" -> Map("badCol" -> "name")) case class Thing(col: String)""",
        "badCol is not a member of Thing"
      )

      success
    }
  }
}
