package com.lucidchart.relate

import java.sql.{Connection, PreparedStatement}

class InterpolatedQuery(protected val parsedQuery: String, protected val params: Seq[Parameter])
    extends Sql
    with MultipleParameter {

  def +(query: InterpolatedQuery) = new InterpolatedQuery(parsedQuery + query.parsedQuery, params ++ query.params)

  protected def applyParams(stmt: PreparedStatement) = parameterize(stmt, 1)

  def placeholder = parsedQuery

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
    val query = StringContext.standardInterpolator(identity, params.map(_.placeholder), parts)
    new InterpolatedQuery(query, params)
  }

}
