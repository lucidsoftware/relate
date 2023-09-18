package com.lucidchart.relate.macros

import com.lucidchart.relate._
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import shapeless.test.illTyped

case class Thing(firstName: String, b: Option[Int])
object Thing {
  def apply(a: String): Thing = new Thing(a, None)
}
case class User(firstName: String, lastName: String)

case class Big(
  f1: Int,
  f2: Option[Int],
  f3: Int,
  f4: Int,
  f5: Int,
  f6: Int,
  f7: Int,
  f8: Int,
  f9: Int,
  z10: Int,
  z11: Int,
  z12: Int,
  z13: Int,
  z14: Int,
  z15: Int,
  z16: Int,
  z17: Int,
  z18: Int,
  z19: Int,
  a20: Int,
  a21: Int,
  a22: Int,
  a23: Int,
  a24: Option[Int],
  a25: Int
) {
  val m1: Int = 0
  def m2: Int = 0
}


class RowParserTest extends Specification with Mockito {
  class MockableRow extends SqlRow(null) {
    final override def apply[A: ColReader](col: String): A = super.apply(col)
    final override def opt[A: ColReader](col: String): Option[A] = super.opt(col)
  }

  "RowParser def macros" should {
    "generate parser" in {
      val row = mock[MockableRow]
      row.stringOption("firstName") returns Some("hi")
      row.intOption("b") returns Some(20)

      val p = generateParser[Thing]

      p.parse(row) mustEqual(Thing("hi", Some(20)))
    }

    "generate parser w/snake_case columns" in {
      val row = mock[MockableRow]
      row.stringOption("first_name") returns Some("gregg")
      row.intOption("b") returns Some(20)

      val p = generateSnakeParser[Thing]

      p.parse(row) mustEqual(Thing("gregg", Some(20)))
    }

    "remap column names" in {
      val p = generateParser[User](Map(
        "firstName" -> "fname",
        "lastName" -> "lname"
      ))

      val row = mock[MockableRow]
      row.stringOption("fname") returns Some("gregg")
      row.stringOption("lname") returns Some("hernandez")

      p.parse(row) mustEqual User("gregg", "hernandez")
    }

    "remap column names w/normal tuple syntax" in {
      val p = generateParser[User](Map(
        ("firstName", "fname"),
        ("lastName", "lname")
      ))

      val row = mock[MockableRow]
      row.stringOption("fname") returns Some("gregg")
      row.stringOption("lname") returns Some("hernandez")

      p.parse(row) mustEqual User("gregg", "hernandez")
    }

    "remap some column names" in {
      val p = generateParser[User](Map(
        "firstName" -> "fname"
      ))

      val row = mock[MockableRow]
      row.stringOption("fname") returns Some("gregg")
      row.stringOption("lastName") returns Some("hernandez")

      p.parse(row) mustEqual User("gregg", "hernandez")
    }

    "generate parser for a case class > 22 fields" in {
      val row = mock[MockableRow]
      for (i <- (1 to 9)) { row.intOption(s"f${i}") returns Some(i) }
      for (i <- (10 to 19)) { row.intOption(s"z${i}") returns Some(i) }
      for (i <- (20 to 25)) { row.intOption(s"a${i}") returns Some(i) }


      val p = generateParser[Big]

      p.parse(row) mustEqual(Big(
        1, Some(2), 3, 4, 5, 6, 7, 8, 9, 10,
        11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, Some(24), 25)
      )
    }

    "fail to compile with non-literals" in {
      illTyped(
        """val name = "newName"; generateParser[User](Map("firstName" -> name))""",
        "Remappings must be literal strings"
      )

      illTyped(
        """val col = "col"; generateParser[User](Map(col -> "name"))""",
        "Column names must be literal strings"
      )

      success
    }

    "fail to compile with invalid columns" in {
      illTyped(
        """generateParser[User](Map("badCol" -> "name"))""",
        "badCol is not a member of com.lucidchart.relate.macros.User"
      )

      success
    }
  }

  "@Record" should {
    @Record()
    case class SimpleRecord(firstName: String, lastName: Option[String])

    "generate parser" in {
      val row = mock[MockableRow]
      row.stringOption("firstName") returns Some("gregg")
      row.stringOption("lastName") returns Some("hernandez")

      implicitly[RowParser[SimpleRecord]].parse(row) mustEqual SimpleRecord("gregg", Some("hernandez"))
    }

    @Record("snakeCase" -> true)
    case class SnakeRecord(firstName: String, lastName: Option[String])

    object SnakeRecord {
      def f(): String = "hello"
    }

    "generate parser w/snake_case columns" in {
      val row = mock[MockableRow]
      row.stringOption("first_name") returns Some("gregg")
      row.stringOption("last_name") returns Some("hernandez")

      // verify that this still compiles
      SnakeRecord.f()

      implicitly[RowParser[SnakeRecord]].parse(row) mustEqual SnakeRecord("gregg", Some("hernandez"))
    }

    @Record("colMapping" -> Map("firstName" -> "fname", "lastName" -> "lname"))
    case class RemapRecord(firstName: String, lastName: String)

    "remap column names" in {
      val row = mock[MockableRow]
      row.stringOption("fname") returns Some("gregg")
      row.stringOption("lname") returns Some("hernandez")

      implicitly[RowParser[RemapRecord]].parse(row) mustEqual RemapRecord("gregg", "hernandez")
    }

    @Record("colMapping" -> Map(("firstName", "fname"), ("lastName", "lname")))
    case class RemapTRecord(firstName: String, lastName: String)

    "remap column names w/normal tuple syntax" in {
      val row = mock[MockableRow]
      row.stringOption("fname") returns Some("gregg")
      row.stringOption("lname") returns Some("hernandez")

      implicitly[RowParser[RemapTRecord]].parse(row) mustEqual RemapTRecord("gregg", "hernandez")
    }

    @Record("colMapping" -> Map("firstName" -> "fname"))
    case class RemapSomeRecord(firstName: String, lastName: String)

    "remap some column names" in {
      val row = mock[MockableRow]
      row.stringOption("fname") returns Some("gregg")
      row.stringOption("lastName") returns Some("hernandez")

      implicitly[RowParser[RemapSomeRecord]].parse(row) mustEqual RemapSomeRecord("gregg", "hernandez")
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
