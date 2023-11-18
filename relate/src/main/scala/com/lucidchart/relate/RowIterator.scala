package com.lucidchart.relate

import java.sql.PreparedStatement

private[relate] object RowIterator {
  def apply[A](parser: SqlRow => A, stmt: PreparedStatement, resultSet: SqlResult) =
    new RowIterator(parser, stmt, resultSet)
}

private[relate] class RowIterator[A](parser: SqlRow => A, stmt: PreparedStatement, result: SqlResult)
    extends Iterator[A] {

  private var _hasNext = result.next()

  /**
   * Make certain that all resources are closed
   */
  override def finalize(): Unit = {
    close()
  }

  /**
   * Determine whether there is another row or not
   * @return
   *   whether there is another row
   */
  override def hasNext: Boolean = _hasNext

  /**
   * Parse the next row using the RowParser passed into the class
   * @return
   *   the parsed record
   */
  override def next(): A = {
    val ret = parser(result.asRow)
    if (_hasNext) {
      _hasNext = result.next()
    }

    // if we've iterated through the whole thing, close resources
    if (!_hasNext) {
      close()
    }
    ret
  }

  /**
   * Close up resources
   */
  private def close(): Unit = {
    if (!stmt.isClosed()) {
      stmt.close()
    }
    if (!result.resultSet.isClosed()) {
      result.resultSet.close()
    }
  }

}
