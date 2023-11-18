package com.lucidchart.relate

import scala.annotation.{compileTimeOnly, implicitNotFound, StaticAnnotation}
import scala.language.implicitConversions

package object macros {
  def generateSnakeParser[A]: RowParser[A] =
    macro RowParserImpl.generateSnakeImpl[A]

  def generateParser[A]: RowParser[A] =
    macro RowParserImpl.generateImpl[A]

  def generateParser[A](colMapping: Map[String, String]): RowParser[A] =
    macro RowParserImpl.generateMappingImpl[A]

  @implicitNotFound("A value of type ${A} is never allowed as an RecordOption")
  private trait RecordOptionValue[A]
  private object RecordOptionValue {
    implicit val bool: RecordOptionValue[Boolean] = new RecordOptionValue[Boolean] {}
    implicit val map: RecordOptionValue[Map[String, String]] = new RecordOptionValue[Map[String, String]] {}
  }

  case class RecordOption[A: RecordOptionValue] private (key: String, value: A)
  object RecordOption {
    implicit def tuple2options[A: RecordOptionValue](t: (String, A)): RecordOption[A] = {
      RecordOption(t._1, t._2)
    }
  }

  @compileTimeOnly("enable macro paradise to expand macro annotations")
  class Record(options: RecordOption[_]*) extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro RowParserImpl.annotation
  }
}
