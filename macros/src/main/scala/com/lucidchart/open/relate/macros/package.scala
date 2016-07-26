package com.lucidchart.open.relate

import scala.annotation.{compileTimeOnly, implicitNotFound, StaticAnnotation}
import scala.language.implicitConversions

package object macros {
  def generateSnakeParseable[A]: Parseable[A] =
    macro ParseableImpl.generateSnakeImpl[A]

  def generateParseable[A]: Parseable[A] =
    macro ParseableImpl.generateImpl[A]

  def generateParseable[A](colMapping: Map[String, String]): Parseable[A] =
    macro ParseableImpl.generateMappingImpl[A]

  @implicitNotFound("A value of type ${A} is never allowed as an RecordOption")
  private trait RecordOptionValue[A]
  private object RecordOptionValue {
    implicit val bool = new RecordOptionValue[Boolean] {}
    implicit val map = new RecordOptionValue[Map[String, String]] {}
  }

  case class RecordOption[A: RecordOptionValue] private (key: String, value: A)
  object RecordOption {
    implicit def tuple2options[A: RecordOptionValue](t: (String, A)): RecordOption[A] = {
      RecordOption(t._1, t._2)
    }
  }

  @compileTimeOnly("enable macro paradise to expand macro annotations")
  class Record(options: RecordOption[_]*) extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro ParseableImpl.annotation
  }
}
