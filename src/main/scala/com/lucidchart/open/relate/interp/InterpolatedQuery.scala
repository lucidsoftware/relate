package com.lucidchart.open.relate.interp

import com.lucidchart.open.relate.Sql
import java.sql.PreparedStatement

import scala.collection.mutable.ListBuffer

class InterpolatedQuery(protected val parsedQuery: String, protected val params: Seq[Parameter]) extends Sql with MultipleParameter {

  def +(query: InterpolatedQuery) = new InterpolatedQuery(parsedQuery + query.parsedQuery, params ++ query.params)

  protected def applyParams(stmt: PreparedStatement) = parameterize(stmt, 1)

  def appendPlaceholders(stringBuilder: StringBuilder) = stringBuilder ++= parsedQuery

}

object InterpolatedQuery {

  def fromParts(parts: Seq[String], params: Seq[Parameter]) = {
    val stringBuilder = new StringBuilder()
    val listBuf = new ListBuffer[Parameter]()

    parts.zip(params).foreach { case (part, param) =>
      if (part.length > 0 && part.takeRight(1) == "#") {
        stringBuilder ++= part.dropRight(1)
        stringBuilder ++= param.toString
      } else {
        stringBuilder ++= part
        param.appendPlaceholders(stringBuilder)
        listBuf += param
      }
    }
    stringBuilder ++= parts.last
    new InterpolatedQuery(stringBuilder.toString(), listBuf)
  }

}
