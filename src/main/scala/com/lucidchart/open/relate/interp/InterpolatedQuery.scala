package com.lucidchart.open.relate.interp

import com.lucidchart.open.relate._
import com.lucidchart.open.relate.Sql
import java.sql.Connection
import java.sql.PreparedStatement

import scala.collection.mutable.ListBuffer

class InterpolatedQuery(protected val parsedQuery: String, protected val params: Seq[Parameter]) extends Sql with MultipleParameter {

  def +(query: InterpolatedQuery) = new InterpolatedQuery(parsedQuery + query.parsedQuery, params ++ query.params)

  protected def applyParams(stmt: PreparedStatement) = parameterize(stmt, 1)

  def appendPlaceholders(stringBuilder: StringBuilder) = stringBuilder ++= parsedQuery

  def withTimeout(seconds: Int): InterpolatedQuery = new InterpolatedQuery(parsedQuery, params) {
    override protected def normalStatement(implicit conn: Connection) = new BaseStatement(conn) with NormalStatementPreparer {
      override def timeout = Some(seconds)
    }

    override protected def insertionStatement(implicit conn: Connection) = new BaseStatement(conn) with InsertionStatementPreparer {
      override def timeout = Some(seconds)
    }

    override protected def streamedStatement(fetchSize: Int)(implicit conn: Connection) = {
      val fetchSize_ = fetchSize
      new BaseStatement(conn) with StreamedStatementPreparer {
        protected val fetchSize = fetchSize_
        override def timeout = Some(seconds)
      }
    }
  }

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
