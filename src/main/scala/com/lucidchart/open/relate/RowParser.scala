package com.lucidchart.open.relate

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
   * Shorthand for creating a RowParser that takes only a BigInt column from the result set
   * @param columnLabel the column name to extract
   * @param the extracted column value
   */
  def bigInt(columnLabel: String) = (row: SqlRow) => row.bigInt(columnLabel)
  /**
   * Shorthand for creating a RowParser that takes only a date column from the result set
   * @param columnLabel the column name to extract
   * @param the extracted column value
   */
  def date(columnLabel: String) = (row: SqlRow) => row.date(columnLabel)
  /**
   * Shorthand for creating a RowParser that takes only an int column from the result set
   * @param columnLabel the column name to extract
   * @param the extracted column value
   */
  def int(columnLabel: String) = (row: SqlRow) => row.int(columnLabel)
  def ints(columnLabel: String) = Parser.list[Int](int(columnLabel))
  /**
   * Shorthand for creating a RowParser that takes only a long column from the result set
   * @param columnLabel the column name to extract
   * @param the extracted column value
   */
  def long(columnLabel: String) = (row: SqlRow) => row.long(columnLabel)
  /**
   * Shorthand for creating a RowParser that takes only a string column from the result set
   * @param columnLabel the column name to extract
   * @param the extracted column value
   */
  def string(columnLabel: String) = (row: SqlRow) => row.string(columnLabel)

  private[relate] def insertInt(row: SqlRow) = row.int(1)
  private[relate] def insertLong(row: SqlRow) = row.long(1)
}
