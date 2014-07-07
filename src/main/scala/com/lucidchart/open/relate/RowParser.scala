package com.lucidchart.open.relate

/**
 * A RowParser is a function that takes a SqlResult as a parameter and parses it to
 * return a concrete type
 *
 * See the [[com.lucidchart.open.relate.RowParser$#apply RowParser]] for more information
 */
trait RowParser[+A] extends (SqlResult => A)

/**
 * The RowParser companion object allows creation of arbitrary RowParsers with its apply method.
 * {{{
 * import com.lucidchart.open.relate.RowParser
 *
 * val rowParser = RowParser { row =>
 *   (row.long("id"), row.string("name"))
 * }
 * }}}
 */
object RowParser {
  /**
   * Create a new RowParser by passing in a function that takes a SqlResult and returns
   * a concrete type
   * @param f the function that will do the parsing
   */
  def apply[A](f: (SqlResult) => A) = new RowParser[A] {
    def apply(row: SqlResult) = f(row)
  }

  /**
   * Shorthand for creating a RowParser that takes only a BigInt column from the result set
   * @param columnLabel the column name to extract
   * @param the extracted column value
   */
  def bigInt(columnLabel: String) = RowParser { row => row.bigInt(columnLabel) }
  /**
   * Shorthand for creating a RowParser that takes only a date column from the result set
   * @param columnLabel the column name to extract
   * @param the extracted column value
   */
  def date(columnLabel: String) = RowParser { row => row.date(columnLabel) }
  /**
   * Shorthand for creating a RowParser that takes only an int column from the result set
   * @param columnLabel the column name to extract
   * @param the extracted column value
   */
  def int(columnLabel: String) = RowParser { row => row.int(columnLabel) }
  /**
   * Shorthand for creating a RowParser that takes only a long column from the result set
   * @param columnLabel the column name to extract
   * @param the extracted column value
   */
  def long(columnLabel: String) = RowParser { row => row.long(columnLabel) }
  /**
   * Shorthand for creating a RowParser that takes only a string column from the result set
   * @param columnLabel the column name to extract
   * @param the extracted column value
   */
  def string(columnLabel: String) = RowParser { row => row.string(columnLabel) }

  private[relate] val insertInt = RowParser { row => row.strictInt(1) }
  private[relate] val insertLong = RowParser { row => row.strictLong(1) }
}
