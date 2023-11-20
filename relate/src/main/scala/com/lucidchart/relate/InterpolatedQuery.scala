package com.lucidchart.relate

import java.sql.{Connection, PreparedStatement}

class InterpolatedQuery(protected val parsedQuery: String, protected val params: Seq[Parameter])
    extends Sql
    with MultipleParameter {

  def +(query: InterpolatedQuery) = new InterpolatedQuery(parsedQuery + query.parsedQuery, params ++ query.params)

  override protected def applyParams(stmt: PreparedStatement): Unit = {
    parameterize(stmt, 1)
    ()
  }

  def appendPlaceholders(stringBuilder: StringBuilder) = stringBuilder ++= parsedQuery

  def withTimeout(seconds: Int): InterpolatedQuery = new InterpolatedQuery(parsedQuery, params) {
    override protected def normalStatement(implicit conn: Connection) = new BaseStatement(conn)
      with NormalStatementPreparer {
      override def timeout = Some(seconds)
    }

    override protected def insertionStatement(implicit conn: Connection) = new BaseStatement(conn)
      with InsertionStatementPreparer {
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
    parts.zip(params).foreach { case (part, param) =>
      stringBuilder ++= part
      param.appendPlaceholders(stringBuilder)
    }
    stringBuilder ++= parts.last
    new InterpolatedQuery(stringBuilder.toString(), params)
  }

}
