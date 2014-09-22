package com.lucidchart.open.relate.interp

import com.lucidchart.open.relate.Sql
import java.sql.PreparedStatement

class InterpolatedQuery(protected val parsedQuery: String, protected val params: Seq[Parameter]) extends Sql with MultipleParameter {
  protected def applyParams(stmt: PreparedStatement) = parameterize(stmt, 1)

  def appendPlaceholders(stringBuilder: StringBuilder) = stringBuilder ++= parsedQuery
}

object InterpolatedQuery {

  def fromParts(parts: Seq[String], params: Seq[Parameter]) = {
    val stringBuilder = new StringBuilder()
    parts.zip(params).foreach { case (part, param) =>
      stringBuilder ++= part
      param.appendPlaceholders(stringBuilder)
    }
    stringBuilder ++= parts.last
    new InterpolatedQuery(stringBuilder.toString(), params)
  }

}
