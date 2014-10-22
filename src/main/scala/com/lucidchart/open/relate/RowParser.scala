package com.lucidchart.open.relate

/**
 * A RowParser is a function that takes a SqlResult as a parameter and parses it to
 * return a concrete type
 *
 * See the [[com.lucidchart.open.relate.RowParser$#apply RowParser]] for more information
 *
 */
@deprecated("Use plain SqlResult => A instead", "1.7.0")
trait RowParser[+A] extends (SqlResult => A)

/**
 * The RowParser companion object allows creation of arbitrary RowParsers with its apply method.
 * {{{
 * import com.lucidchart.open.relate.RowParser
 *
 * val rowParser = (row: SqlResult) =>
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
  @deprecated("Use plain SqlResult => A instead", "1.7.0")
  def apply[A](f: (SqlResult) => A) = new RowParser[A] {
    def apply(row: SqlResult) = f(row)
  }

  /**
   * Shorthand for creating a RowParser that takes only a BigInt column from the result set
   * @param columnLabel the column name to extract
   * @param the extracted column value
   */
  def bigInt(columnLabel: String) = (row: SqlResult) => row.bigInt(columnLabel)
  /**
   * Shorthand for creating a RowParser that takes only a date column from the result set
   * @param columnLabel the column name to extract
   * @param the extracted column value
   */
  def date(columnLabel: String) = (row: SqlResult) => row.date(columnLabel)
  /**
   * Shorthand for creating a RowParser that takes only an int column from the result set
   * @param columnLabel the column name to extract
   * @param the extracted column value
   */
  def int(columnLabel: String) = (row: SqlResult) => row.int(columnLabel)
  /**
   * Shorthand for creating a RowParser that takes only a long column from the result set
   * @param columnLabel the column name to extract
   * @param the extracted column value
   */
  def long(columnLabel: String) = (row: SqlResult) => row.long(columnLabel)
  /**
   * Shorthand for creating a RowParser that takes only a string column from the result set
   * @param columnLabel the column name to extract
   * @param the extracted column value
   */
  def string(columnLabel: String) = (row: SqlResult) => row.string(columnLabel)

  private[relate] val insertInt = (row: SqlResult) => row.strictInt(1)
  private[relate] val insertLong = (row: SqlResult) => row.strictLong(1)
}
