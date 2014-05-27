package com.lucidchart.open.relate

import java.sql.{Date => SqlDate, PreparedStatement, Statement, Timestamp, Types}
import java.math.{BigDecimal => JBigDecimal, BigInteger => JBigInt}
import java.util.{Date, UUID}
import scala.reflect.ClassTag

/** 
 * This object provides syntactic sugar so that the implicit SqlStatement object can
 * be called without always using statement.whatever()
 */
object SqlTypes {
  def bigDecimal(name: String, value: BigDecimal)(implicit stmt: SqlStatement) = stmt.bigDecimal(name, value)
  def bigDecimal(name: String, value: JBigDecimal)(implicit stmt: SqlStatement) = stmt.bigDecimal(name, value)
  def bigDecimalOption[A](name: String, value: Option[A])(implicit stmt: SqlStatement, bd: BigDecimalLike[A]) = stmt.bigDecimalOption(name, value)
  def bigInt(name: String, value: BigInt)(implicit stmt: SqlStatement) = stmt.bigInt(name, value)
  def bigInt(name: String, value: JBigInt)(implicit stmt: SqlStatement) = stmt.bigInt(name, value)
  def bigIntOption[A](name: String, value: Option[A])(implicit stmt: SqlStatement, bi: BigIntLike[A]) = stmt.bigIntOption(name, value)
  def bool(name: String, value: Boolean)(implicit stmt: SqlStatement) = stmt.bool(name, value)
  def boolOption[A](name: String, value: Option[Boolean])(implicit stmt: SqlStatement) = stmt.boolOption(name, value)
  def byte(name: String, value: Byte)(implicit stmt: SqlStatement) = stmt.byte(name, value)
  def byteOption(name: String, value: Option[Byte])(implicit stmt: SqlStatement) = stmt.byteOption(name, value)
  def char(name: String, value: Char)(implicit stmt: SqlStatement) = stmt.char(name, value)
  def charOption(name: String, value: Option[Char])(implicit stmt: SqlStatement) = stmt.charOption(name, value)
  def date(name: String, value: Date)(implicit stmt: SqlStatement) = stmt.date(name, value)
  def dateOption(name: String, value: Option[Date])(implicit stmt: SqlStatement) = stmt.dateOption(name, value)
  def double(name: String, value: Double)(implicit stmt: SqlStatement) = stmt.double(name, value)
  def doubleOption(name: String, value: Option[Double])(implicit stmt: SqlStatement) = stmt.doubleOption(name, value)
  def float(name: String, value: Float)(implicit stmt: SqlStatement) = stmt.float(name, value)
  def floatOption(name: String, value: Option[Float])(implicit stmt: SqlStatement) = stmt.floatOption(name, value)
  def int(name: String, value: Int)(implicit stmt: SqlStatement) = stmt.int(name, value)
  def intOption(name: String, value: Option[Int])(implicit stmt: SqlStatement) = stmt.intOption(name, value)
  def long(name: String, value: Long)(implicit stmt: SqlStatement) = stmt.long(name, value)
  def longOption(name: String, value: Option[Long])(implicit stmt: SqlStatement) = stmt.longOption(name, value)
  def short(name: String, value: Short)(implicit stmt: SqlStatement) = stmt.short(name, value)
  def shortOption(name: String, value: Option[Short])(implicit stmt: SqlStatement) = stmt.shortOption(name, value)
  def string(name: String, value: String)(implicit stmt: SqlStatement) = stmt.string(name, value)
  def stringOption(name: String, value: Option[String])(implicit stmt: SqlStatement) = stmt.stringOption(name, value)
  def timestamp(name: String, value: Timestamp)(implicit stmt: SqlStatement) = stmt.timestamp(name, value)
  def timestampOption(name: String, value: Option[Timestamp])(implicit stmt: SqlStatement) = stmt.timestampOption(name, value)
  def uuid(name: String, value: UUID)(implicit stmt: SqlStatement) = stmt.uuid(name, value)
  def uuidOption(name: String, value: Option[UUID])(implicit stmt: SqlStatement) = stmt.uuidOption(name, value)

  trait BigDecimalLike[A] {
    def get(value: A): JBigDecimal
  }

  implicit object BigDecimalWrap extends BigDecimalLike[BigDecimal] {
    def get(value: BigDecimal): JBigDecimal = value.bigDecimal
  }

  implicit object JBigDecimalWrap extends BigDecimalLike[JBigDecimal] {
    def get(value: JBigDecimal): JBigDecimal = value
  }

  trait BigIntLike[A] {
    def get(value: A): JBigInt
  }

  implicit object BigIntWrap extends BigIntLike[BigInt] {
    def get(value: BigInt): JBigInt = value.bigInteger
  }

  implicit object JBigIntWrap extends BigIntLike[JBigInt] {
    def get(value: JBigInt): JBigInt = value
  }
}

/**
 * A smart wrapper around the PreparedStatement class that allows inserting
 * parameter values by name rather than by index. Provides methods for inserting
 * all necessary datatypes.
 */
class SqlStatement(stmt: PreparedStatement, names: Map[String, Int]) {

  def list[A](name: String, values: TraversableOnce[A], rule: (Int, A) => Unit) {
    var i = names(name)
    val iterator = values.toIterator
    while (iterator.hasNext) {
      rule(i, iterator.next())
      i += 1
    } 
  }

  /**
   * Set a BigDecimal in the PreparedStatement
   * @param name the name of the parameter to put the BigDecimal in
   * @param value the BigDecimal to put in the query
   */
  def bigDecimal(name: String, value: BigDecimal): Unit = {
    bigDecimal(name, value.bigDecimal)
  }
  def bigDecimal(name: String, values: TraversableOnce[BigDecimal]): Unit = list[JBigDecimal](name, values.map(_.bigDecimal), stmt.setBigDecimal _)

  /**
   * Set a Java BigDecimal in the PreparedStatement
   * @param name the name of the parameter to put the BigDecimal in 
   * @param value the BigDecimal to put in the query
   */
  def bigDecimal(name: String, value: JBigDecimal): Unit = {
    stmt.setBigDecimal(names(name), value)
  }
  def bigDecimal[X: ClassTag](name: String, values: TraversableOnce[JBigDecimal]): Unit = list[JBigDecimal](name, values, stmt.setBigDecimal _)

  def bigDecimalOption[A](name: String, value: Option[A])(implicit bd: SqlTypes.BigDecimalLike[A]): Unit = {
    value.map(d => bigDecimal(name, bd.get(d))).getOrElse(stmt.setNull(names(name), Types.DECIMAL))
  }

  /**
   * Set a BigInt in the PreparedStatement
   * @param name the name of the parameter to put the BigInt in 
   * @param value the BigInt to put into the query
   */
  def bigInt(name: String, value: BigInt): Unit = {
    stmt.setBigDecimal(names(name), new JBigDecimal(value.bigInteger))
  }
  def bigInt(name: String, values: TraversableOnce[BigInt]): Unit = list[JBigDecimal](name, values.map{ i: BigInt => new JBigDecimal(i.bigInteger) }, stmt.setBigDecimal _)

  /**
   * Set a Java BigInteger in the PreparedStatement
   * @param name the name of the parameter to put the BigInteger in
   * @param value the BigInteger to put in the query
   */
  def bigInt(name: String, value: JBigInt): Unit = {
    stmt.setBigDecimal(names(name), new JBigDecimal(value))
  }
  def bigInt[X: ClassTag](name: String, values: TraversableOnce[JBigInt]): Unit = list[JBigDecimal](name, values.map(new JBigDecimal(_)), stmt.setBigDecimal _)

  def bigIntOption[A](name: String, value: Option[A])(implicit bd: SqlTypes.BigIntLike[A]): Unit = {
    value.map(i => bigInt(name, bd.get(i))).getOrElse(stmt.setNull(names(name), Types.BIGINT))
  }

  /**
   * Set a Boolean in the PreparedStatement
   * @param name the name of the parameter to put the Boolean in
   * @param value the Boolean to put in the query
   */
  def bool(name: String, value: Boolean): Unit = {
    stmt.setBoolean(names(name), value)
  }
  def bool(name: String, values: TraversableOnce[Boolean]): Unit = list[Boolean](name, values, stmt.setBoolean _)

  def boolOption(name: String, value: Option[Boolean]): Unit = {
    value.map(bool(name, _)).getOrElse(stmt.setNull(names(name), Types.BOOLEAN))
  }

  /**
   * Set a Byte in the PreparedStatement
   * @param name the name of the parameter to put the Byte in
   * @param value the Byte to put in the query
   */
  def byte(name: String, value: Byte): Unit = {
    stmt.setByte(names(name), value)
  }
  def byte(name: String, values: TraversableOnce[Byte]): Unit = list[Byte](name, values, stmt.setByte _)

  def byteOption(name: String, value: Option[Byte]): Unit = {
    value.map(byte(name, _)).getOrElse(stmt.setNull(names(name), Types.TINYINT))
  }

  /**
   * Set a Char in the PreparedStatement
   * @param name the name of the parameter to put the Char in 
   * @param value the Char to put in the query
   */
  def char(name: String, value: Char): Unit = {
    stmt.setString(names(name), value.toString)
  }
  def char(name: String, values: TraversableOnce[Char]): Unit = list[String](name, values.map(_.toString), stmt.setString _)

  def charOption(name: String, value: Option[Char]): Unit = {
    value.map(char(name, _)).getOrElse(stmt.setNull(names(name), Types.CHAR))
  }

  /**
   * Set a Date in the PreparedStatement
   * @param name the name of the parameter to put the Date in
   * @param value the Date to put in the query
   */
  def date(name: String, value: Date): Unit = {
    if (value != null) stmt.setDate(names(name), new SqlDate(value.getTime))
    else stmt.setNull(names(name), Types.DATE)
  }
  def date(name: String, values: TraversableOnce[Date]): Unit = list[SqlDate](name, values.map{ d: Date => new SqlDate(d.getTime)}, stmt.setDate _)

  def dateOption(name: String, value: Option[Date]): Unit = {
    value.map(date(name, _)).getOrElse(stmt.setNull(names(name), Types.DATE))
  }

  /**
   * Set a Double in the PreparedStatement
   * @param name the name of the parameter to put the Double in
   * @param value the Double to put in the query
   */
  def double(name: String, value: Double): Unit = {
    stmt.setDouble(names(name), value)
  }
  def double(name: String, values: TraversableOnce[Double]): Unit = list[Double](name, values, stmt.setDouble _)

  def doubleOption(name: String, value: Option[Double]): Unit = {
    value.map(double(name, _)).getOrElse(stmt.setNull(names(name), Types.DOUBLE))
  }

  /**
   * Set a Float in the PreparedStatement
   * @param name the name of the parameter to put the Float in
   * @param value the Float to put in the query
   */
  def float(name: String, value: Float): Unit = {
    stmt.setFloat(names(name), value)
  }
  def float(name: String, values: TraversableOnce[Float]): Unit = list[Float](name, values, stmt.setFloat _)

  def floatOption(name: String, value: Option[Float]): Unit = {
    value.map(float(name, _)).getOrElse(stmt.setNull(names(name), Types.FLOAT))
  }

  /**
   * Set an Int in the PreparedStatement
   * @param name the name of the parameter to put the int in
   * @param value the int to put in the query
   */
  def int(name: String, value: Int): Unit = {
    stmt.setInt(names(name), value)
  }
  def int(name: String, values: TraversableOnce[Int]): Unit = list[Int](name, values, stmt.setInt _)

  def intOption(name: String, value: Option[Int]): Unit = {
    value.map(int(name, _)).getOrElse(stmt.setNull(names(name), Types.INTEGER))
  }

  /**
   * Set a Long in the PreparedStatement
   * @param name the name of the parameter to put the Long in
   * @param value the Long to put in the query
   */
  def long(name: String, value: Long): Unit = {
    stmt.setLong(names(name), value)
  }
  def long(name: String, values: TraversableOnce[Long]): Unit = list[Long](name, values, stmt.setLong _)

  def longOption(name: String, value: Option[Long]): Unit = {
    value.map(long(name, _)).getOrElse(stmt.setNull(names(name), Types.BIGINT))
  }

  /**
   * Set a Short in the PreparedStatement
   * @param name the name of the parameter to put the Short in
   * @param value the Short to put in the query
   */
  def short(name: String, value: Short): Unit = {
    stmt.setShort(names(name), value)
  }
  def short(name: String, values: TraversableOnce[Short]): Unit = list[Short](name, values, stmt.setShort _)

  def shortOption(name: String, value: Option[Short]): Unit = {
    value.map(short(name, _)).getOrElse(stmt.setNull(names(name), Types.SMALLINT))
  }

  /**
   * Set a String in the PreparedStatement
   * @param name the name of the parameter to put the string in
   * @param value the value to put in the query
   */
  def string(name: String, value: String): Unit = {
    stmt.setString(names(name), value)
  }

  def string(name: String, values: TraversableOnce[String]): Unit = list[String](name, values, stmt.setString _)

  def stringOption(name: String, value: Option[String]): Unit = {
    value.map(string(name, _)).getOrElse(stmt.setNull(names(name), Types.VARCHAR))
  }

  /**
   * Set a Timestamp in the PreparedStatement
   * @param name the name of the parameter to put the Timestamp in
   * @param value the Timestamp to put into the query
   */
  def timestamp(name: String, value: Timestamp): Unit = {
    stmt.setTimestamp(names(name), value)
  }
  def timestamp(name: String, values: TraversableOnce[Timestamp]): Unit = list[Timestamp](name, values, stmt.setTimestamp _)

  def timestampOption(name: String, value: Option[Timestamp]): Unit = {
    value.map(timestamp(name, _)).getOrElse(stmt.setNull(names(name), Types.TIMESTAMP))
  }

  /**
   * Set a UUID in the PreparedStatement
   * @param name the name of the parameter to put the UUID in
   * @param value the UUID to put in the query
   */
  def uuid(name: String, value: UUID): Unit = {
    if (value != null) stmt.setString(names(name), value.toString)
    else stmt.setNull(names(name), Types.VARCHAR)
  }
  def uuid(name: String, values: TraversableOnce[UUID]): Unit = list[String](name, values.map(_.toString), stmt.setString _)

  def uuidOption(name: String, value: Option[UUID]): Unit = {
    value.map(uuid(name, _)).getOrElse(stmt.setNull(names(name), Types.VARCHAR))
  }

  def setNull(name: String, typ: Int): Unit = {
    stmt.setNull(names(name), typ)
  }

  /**
   * Execute a statement
   */
  def execute(): Boolean = {
    stmt.execute()
  }

  /**
   * Execute an update
   */
  def executeUpdate(): Int = {
    stmt.executeUpdate()
  }

  /**
   * Execute a query
   */
  def executeQuery(): SqlResult = {
    SqlResult(stmt.executeQuery())
  }

  /**
   * Execute an insert
   */
  def executeInsert(): SqlResult = {
    stmt.executeUpdate()
    SqlResult(stmt.getGeneratedKeys())
  }

}