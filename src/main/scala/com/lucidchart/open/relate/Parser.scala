package com.lucidchart.open.relate

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable
import scala.language.implicitConversions

trait Parser[A] {
  def parse(row: SqlRow): A

  val selfContained: Boolean = false

  def map[B](f: A => B): Parser[B] = Parser[B] { row =>
    f(parse(row))
  }

  private[relate] def doParse(row: SqlRow): A = {
    if (selfContained) {
      parse(row)
    } else {
      row.resultSet.next()
      parse(row)
    }
  }
}

object ScParser {
  def apply[A](f: SqlRow => A): Parser[A] = new Parser[A] {
    override val selfContained: Boolean = true
    def parse(row: SqlRow): A = f(row)
  }
}

object Parser {
  def apply[A](f: SqlRow => A): Parser[A] = new Parser[A] {
    def parse(row: SqlRow): A = f(row)
  }

  implicit def promote[A](f: SqlRow => A): Parser[A] = Parser(f)
  implicit def demote[A](p: Parser[A]): SqlRow => A = p.parse _

  def limitedCollection[B: Parser, Col[_]](maxRows: Long)(implicit cbf: CanBuildFrom[Col[B], B, Col[B]]) =
    ScParser { row =>
      val builder = cbf()

      while (row.resultSet.getRow < maxRows && row.resultSet.next()) {
        builder += implicitly[Parser[B]].parse(row)
      }

      builder.result
    }

  def scalar[A: Parser: Numeric] = scalarOption[A].map(_.get)
  def scalarOption[A: Parser: Numeric] = ScParser[Option[A]] { row =>
    if (row.next()) {
      Some(row.resultSet.getObject(1).asInstanceOf[A])
    } else {
      None
    }
  }

  implicit def option[B: Parser] = ScParser[Option[B]] { result =>
    limitedCollection[B, List](1).parse(result).headOption
  }

  implicit def collection[B: Parser, Col[_]](implicit cbf: CanBuildFrom[Col[B], B, Col[B]]) =
    limitedCollection[B, Col](Long.MaxValue)

  def list[A: Parser] = collection[A, List]
  def listOf[A](parser: Parser[A]) = collection[A, List](
    parser,
    implicitly[CanBuildFrom[List[A], A, List[A]]]
  )

  implicit def pairCollection[Key: Parser, Value: Parser, PairCol[_, _]]
    (implicit cbf: CanBuildFrom[PairCol[Key, Value], (Key, Value), PairCol[Key, Value]]) =
    ScParser { row =>

      val builder = cbf()

      while (row.resultSet.getRow < Long.MaxValue && row.resultSet.next()) {
        builder += implicitly[Parser[Key]].parse(row) -> implicitly[Parser[Value]].parse(row)
      }

      builder.result
    }

  implicit def multiMap[Key: Parser, Value: Parser] = ScParser[Map[Key, Set[Value]]] { row =>
    val mm: mutable.Map[Key, Set[Value]] = new mutable.HashMap[Key, Set[Value]]

    while (row.resultSet.next()) {
      val key = implicitly[Parser[Key]].parse(row)
      val value = implicitly[Parser[Value]].parse(row)

      mm.get(key).map { foundValue =>
        mm += (key -> (foundValue + value))
      }.getOrElse {
        mm += (key -> Set(value))
      }
    }
    mm.toMap
  }

}