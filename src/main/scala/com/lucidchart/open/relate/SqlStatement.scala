package com.lucidchart.open.relate

import java.sql.{Date => SqlDate, PreparedStatement, Timestamp, Types}
import java.math.{BigDecimal => JBigDecimal, BigInteger => JBigInt}
import java.util.{Date, UUID}

/** 
 * This object provides syntactic sugar so that the implicit SqlStatement object can
 * be called without always using statement.whatever()
 */
object SqlTypes {
  def bigDecimal(name: String, value: BigDecimal)(implicit stmt: SqlStatement) = stmt.bigDecimal(name, value)
  def bigDecimal(name: String, value: JBigDecimal)(implicit stmt: SqlStatement) = stmt.bigDecimal(name, value)
  def bigInt(name: String, value: BigInt)(implicit stmt: SqlStatement) = stmt.bigInt(name, value)
  def bigInt(name: String, value: JBigInt)(implicit stmt: SqlStatement) = stmt.bigInt(name, value)
  def bool(name: String, value: Boolean)(implicit stmt: SqlStatement) = stmt.bool(name, value)
  def byte(name: String, value: Byte)(implicit stmt: SqlStatement) = stmt.byte(name, value)
  def char(name: String, value: Char)(implicit stmt: SqlStatement) = stmt.char(name, value)
  def date(name: String, value: Date)(implicit stmt: SqlStatement) = stmt.date(name, value)
  def double(name: String, value: Double)(implicit stmt: SqlStatement) = stmt.double(name, value)
  def float(name: String, value: Float)(implicit stmt: SqlStatement) = stmt.float(name, value)
  def int(name: String, value: Int)(implicit stmt: SqlStatement) = stmt.int(name, value)
  def long(name: String, value: Long)(implicit stmt: SqlStatement) = stmt.long(name, value)
  def short(name: String, value: Short)(implicit stmt: SqlStatement) = stmt.short(name, value)
  def string(name: String, value: String)(implicit stmt: SqlStatement) = stmt.string(name, value)
  def timestamp(name: String, value: Timestamp)(implicit stmt: SqlStatement) = stmt.timestamp(name, value)
  def uuid(name: String, value: UUID)(implicit stmt: SqlStatement) = stmt.uuid(name, value)
}

/**
 * A smart wrapper around the PreparedStatement class that allows inserting
 * parameter values by name rather than by index. Provides methods for inserting
 * all necessary datatypes.
 */
class SqlStatement(stmt: PreparedStatement, query: String, names: List[String]) {

  /**
   * Set a BigDecimal in the PreparedStatement
   * @param name the name of the parameter to put the BigDecimal in
   * @param value the BigDecimal to put in the query
   */
  def bigDecimal(name: String, value: BigDecimal): Unit = {
    bigDecimal(name, value.bigDecimal)
  }

  /**
   * Set a Java BigDecimal in the PreparedStatement
   * @param name the name of the parameter to put the BigDecimal in 
   * @param value the BigDecimal to put in the query
   */
  def bigDecimal(name: String, value: JBigDecimal): Unit = {
    stmt.setBigDecimal(names.indexOf(name) + 1, value)
  }

  /**
   * Set a BigInt in the PreparedStatement
   * @param name the name of the parameter to put the BigInt in 
   * @param value the BigInt to put into the query
   */
  def bigInt(name: String, value: BigInt): Unit = {
    stmt.setBigDecimal(names.indexOf(name) + 1, new JBigDecimal(value.bigInteger))
  }

  /**
   * Set a Java BigInteger in the PreparedStatement
   * @param name the name of the parameter to put the BigInteger in
   * @param value the BigInteger to put in the query
   */
  def bigInt(name: String, value: JBigInt): Unit = {
    stmt.setBigDecimal(names.indexOf(name) + 1, new JBigDecimal(value))
  }

  /**
   * Set a Boolean in the PreparedStatement
   * @param name the name of the parameter to put the Boolean in
   * @param value the Boolean to put in the query
   */
  def bool(name: String, value: Boolean): Unit = {
    stmt.setBoolean(names.indexOf(name) + 1, value)
  }

  /**
   * Set a Byte in the PreparedStatement
   * @param name the name of the parameter to put the Byte in
   * @param value the Byte to put in the query
   */
  def byte(name: String, value: Byte): Unit = {
    stmt.setByte(names.indexOf(name) + 1, value)
  }

  /**
   * Set a Char in the PreparedStatement
   * @param name the name of the parameter to put the Char in 
   * @param value the Char to put in the query
   */
  def char(name: String, value: Char): Unit = {
    stmt.setString(names.indexOf(name) + 1, value.toString)
  }

  /**
   * Set a Date in the PreparedStatement
   * @param name the name of the parameter to put the Date in
   * @param value the Date to put in the query
   */
  def date(name: String, value: Date): Unit = {
    if (value != null) stmt.setDate(names.indexOf(name) + 1, new SqlDate(value.getTime))
    else stmt.setNull(names.indexOf(name) + 1, Types.DATE)
  }

  /**
   * Set a Double in the PreparedStatement
   * @param name the name of the parameter to put the Double in
   * @param value the Double to put in the query
   */
  def double(name: String, value: Double): Unit = {
    stmt.setDouble(names.indexOf(name) + 1, value)
  }

  /**
   * Set a Float in the PreparedStatement
   * @param name the name of the parameter to put the Float in
   * @param value the Float to put in the query
   */
  def float(name: String, value: Float): Unit = {
    stmt.setFloat(names.indexOf(name) + 1, value)
  }

  /**
   * Set an Int in the PreparedStatement
   * @param name the name of the parameter to put the int in
   * @param value the int to put in the query
   */
  def int(name: String, value: Int): Unit = {
    stmt.setInt(names.indexOf(name) + 1, value)
  }

  /**
   * Set a Long in the PreparedStatement
   * @param name the name of the parameter to put the Long in
   * @param value the Long to put in the query
   */
  def long(name: String, value: Long): Unit = {
    stmt.setLong(names.indexOf(name) + 1, value)
  }

  /**
   * Set a Short in the PreparedStatement
   * @param name the name of the parameter to put the Short in
   * @param value the Short to put in the query
   */
  def short(name: String, value: Short): Unit = {
    stmt.setShort(names.indexOf(name) + 1, value)
  }

  /**
   * Set a String in the PreparedStatement
   * @param name the name of the parameter to put the string in
   * @param value the value to put in the query
   */
  def string(name: String, value: String): Unit = {
    stmt.setString(names.indexOf(name) + 1, value)
  }

  /**
   * Set a Timestamp in the PreparedStatement
   * @param name the name of the parameter to put the Timestamp in
   * @param value the Timestamp to put into the query
   */
  def timestamp(name: String, value: Timestamp): Unit = {
    stmt.setTimestamp(names.indexOf(name) + 1, value)
  }

  /**
   * Set a UUID in the PreparedStatement
   * @param name the name of the parameter to put the UUID in
   * @param value the UUID to put in the query
   */
  def uuid(name: String, value: UUID): Unit = {
    if (value != null) stmt.setString(names.indexOf(name) + 1, value.toString)
    else stmt.setNull(names.indexOf(name) + 1, Types.VARCHAR)
  }


  def executeUpdate() {
    stmt.executeUpdate()
  }

}