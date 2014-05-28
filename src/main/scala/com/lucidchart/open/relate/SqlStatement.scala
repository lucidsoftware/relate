package com.lucidchart.open.relate

import java.sql.{Date => SqlDate, PreparedStatement, Statement, Timestamp, Types}
import java.math.{BigDecimal => JBigDecimal, BigInteger => JBigInt}
import java.util.{Date, UUID}
import scala.reflect.ClassTag
import java.nio.ByteBuffer

/**
 * A smart wrapper around the PreparedStatement class that allows inserting
 * parameter values by name rather than by index. Provides methods for inserting
 * all necessary datatypes.
 */
class SqlStatement(stmt: PreparedStatement, names: scala.collection.Map[String, List[Int]]) {

  def list[A](name: String, values: TraversableOnce[A], rule: (Int, A) => Unit) {
    var iterator1 = names(name).toIterator
    while(iterator1.hasNext) {
      var i = iterator1.next
      val iterator2 = values.toIterator
      while (iterator2.hasNext) {
        rule(i, iterator2.next())
        i += 1
      }
    }
  }

  def insert[A](name: String, value: A, rule: (Int, A) => Unit) {
    val iterator = names(name).toIterator
    while(iterator.hasNext) {
      rule(iterator.next, value)
    }
  }

  /**
   * Set a BigDecimal in the PreparedStatement
   * @param name the name of the parameter to put the BigDecimal in
   * @param value the BigDecimal to put in the query
   */
  def bigDecimal(name: String, value: BigDecimal) = insert(name, value.bigDecimal, stmt.setBigDecimal _)
  def bigDecimal(name: String, values: TraversableOnce[BigDecimal]): Unit = list[JBigDecimal](name, values.map(_.bigDecimal), stmt.setBigDecimal _)

  /**
   * Set a Java BigDecimal in the PreparedStatement
   * @param name the name of the parameter to put the BigDecimal in 
   * @param value the BigDecimal to put in the query
   */
  def bigDecimal(name: String, value: JBigDecimal) = insert(name, value, stmt.setBigDecimal _)
  def bigDecimal[X: ClassTag](name: String, values: TraversableOnce[JBigDecimal]): Unit = list[JBigDecimal](name, values, stmt.setBigDecimal _)
  def bigDecimalOption[A](name: String, value: Option[A])(implicit bd: Query.BigDecimalLike[A]): Unit = {
    value.map(d => bigDecimal(name, bd.get(d))).getOrElse(insert(name, Types.DECIMAL, stmt.setNull _))
  }

  /**
   * Set a BigInt in the PreparedStatement
   * @param name the name of the parameter to put the BigInt in 
   * @param value the BigInt to put into the query
   */
  def bigInt(name: String, value: BigInt) = insert(name, new JBigDecimal(value.bigInteger), stmt.setBigDecimal _)
  def bigInt(name: String, values: TraversableOnce[BigInt]): Unit = list[JBigDecimal](name, values.map{ i: BigInt => new JBigDecimal(i.bigInteger) }, stmt.setBigDecimal _)

  /**
   * Set a Java BigInteger in the PreparedStatement
   * @param name the name of the parameter to put the BigInteger in
   * @param value the BigInteger to put in the query
   */
  def bigInt(name: String, value: JBigInt) = insert(name, new JBigDecimal(value), stmt.setBigDecimal _)
  def bigInt[X: ClassTag](name: String, values: TraversableOnce[JBigInt]): Unit = list[JBigDecimal](name, values.map(new JBigDecimal(_)), stmt.setBigDecimal _)
  def bigIntOption[A](name: String, value: Option[A])(implicit bd: Query.BigIntLike[A]): Unit = {
    value.map(i => bigInt(name, bd.get(i))).getOrElse(insert(name, Types.BIGINT, stmt.setNull _))
  }

  /**
   * Set a Boolean in the PreparedStatement
   * @param name the name of the parameter to put the Boolean in
   * @param value the Boolean to put in the query
   */
  def bool(name: String, value: Boolean) = (name, value, stmt.setBoolean _)
  def bool(name: String, values: TraversableOnce[Boolean]): Unit = list[Boolean](name, values, stmt.setBoolean _)
  def boolOption(name: String, value: Option[Boolean]): Unit = {
    value.map(bool(name, _)).getOrElse(insert(name, Types.BOOLEAN, stmt.setNull _))
  }

  /**
   * Set a Byte in the PreparedStatement
   * @param name the name of the parameter to put the Byte in
   * @param value the Byte to put in the query
   */
  def byte(name: String, value: Byte) = insert(name, value, stmt.setByte _)
  def byte(name: String, values: TraversableOnce[Byte]): Unit = list[Byte](name, values, stmt.setByte _)
  def byteOption(name: String, value: Option[Byte]): Unit = {
    value.map(byte(name, _)).getOrElse(insert(name, Types.TINYINT, stmt.setNull _))
  }

  /**
   * Set a Char in the PreparedStatement
   * @param name the name of the parameter to put the Char in 
   * @param value the Char to put in the query
   */
  def char(name: String, value: Char) = insert(name, value.toString, stmt.setString _)
  def char(name: String, values: TraversableOnce[Char]): Unit = list[String](name, values.map(_.toString), stmt.setString _)
  def charOption(name: String, value: Option[Char]): Unit = {
    value.map(char(name, _)).getOrElse(insert(name, Types.CHAR, stmt.setNull _))
  }

  /**
   * Set a Date in the PreparedStatement
   * @param name the name of the parameter to put the Date in
   * @param value the Date to put in the query
   */
  def date(name: String, value: Date) = insert(name, new SqlDate(value.getTime), stmt.setDate _)
  def date(name: String, values: TraversableOnce[Date]): Unit = list[SqlDate](name, values.map{ d: Date => new SqlDate(d.getTime)}, stmt.setDate _)
  def dateOption(name: String, value: Option[Date]): Unit = {
    value.map(date(name, _)).getOrElse(insert(name, Types.DATE, stmt.setNull _))
  }

  /**
   * Set a Double in the PreparedStatement
   * @param name the name of the parameter to put the Double in
   * @param value the Double to put in the query
   */
  def double(name: String, value: Double) = insert(name, value, stmt.setDouble _)
  def double(name: String, values: TraversableOnce[Double]): Unit = list[Double](name, values, stmt.setDouble _)
  def doubleOption(name: String, value: Option[Double]): Unit = {
    value.map(double(name, _)).getOrElse(insert(name, Types.DOUBLE, stmt.setNull _))
  }

  /**
   * Set a Float in the PreparedStatement
   * @param name the name of the parameter to put the Float in
   * @param value the Float to put in the query
   */
  def float(name: String, value: Float) = insert(name, value, stmt.setFloat _)
  def float(name: String, values: TraversableOnce[Float]): Unit = list[Float](name, values, stmt.setFloat _)
  def floatOption(name: String, value: Option[Float]): Unit = {
    value.map(float(name, _)).getOrElse(insert(name, Types.FLOAT, stmt.setNull _))
  }

  /**
   * Set an Int in the PreparedStatement
   * @param name the name of the parameter to put the int in
   * @param value the int to put in the query
   */
  def int(name: String, value: Int) = insert(name, value, stmt.setInt _)
  def int(name: String, values: TraversableOnce[Int]): Unit = list[Int](name, values, stmt.setInt _)
  def intOption(name: String, value: Option[Int]): Unit = {
    value.map(int(name, _)).getOrElse(insert(name, Types.INTEGER, stmt.setNull _))
  }

  /**
   * Set a Long in the PreparedStatement
   * @param name the name of the parameter to put the Long in
   * @param value the Long to put in the query
   */
  def long(name: String, value: Long) = insert(name, value, stmt.setLong)
  def long(name: String, values: TraversableOnce[Long]): Unit = list[Long](name, values, stmt.setLong _)
  def longOption(name: String, value: Option[Long]): Unit = {
    value.map(long(name, _)).getOrElse(insert(name, Types.BIGINT, stmt.setNull _))
  }

  /**
   * Set a Short in the PreparedStatement
   * @param name the name of the parameter to put the Short in
   * @param value the Short to put in the query
   */
  def short(name: String, value: Short) = insert(name, value, stmt.setShort _)
  def short(name: String, values: TraversableOnce[Short]): Unit = list[Short](name, values, stmt.setShort _)
  def shortOption(name: String, value: Option[Short]): Unit = {
    value.map(short(name, _)).getOrElse(insert(name, Types.SMALLINT, stmt.setNull _))
  }

  /**
   * Set a String in the PreparedStatement
   * @param name the name of the parameter to put the string in
   * @param value the value to put in the query
   */
  def string(name: String, value: String) = insert(name, value, stmt.setString _)
  def string(name: String, values: TraversableOnce[String]): Unit = list[String](name, values, stmt.setString _)
  def stringOption(name: String, value: Option[String]): Unit = {
    value.map(string(name, _)).getOrElse(insert(name, Types.VARCHAR, stmt.setNull _))
  }

  /**
   * Set a Timestamp in the PreparedStatement
   * @param name the name of the parameter to put the Timestamp in
   * @param value the Timestamp to put into the query
   */
  def timestamp(name: String, value: Timestamp) = insert(name, value, stmt.setTimestamp)
  def timestamp(name: String, values: TraversableOnce[Timestamp]): Unit = list[Timestamp](name, values, stmt.setTimestamp _)
  def timestampOption(name: String, value: Option[Timestamp]): Unit = {
    value.map(timestamp(name, _)).getOrElse(insert(name, Types.TIMESTAMP, stmt.setNull _))
  }

  /**
   * Set a UUID in the PreparedStatement
   * @param name the name of the parameter to put the UUID in
   * @param value the UUID to put in the query
   */
  def uuid(name: String, value: UUID) = insert(name, uuidToByteArray(value), stmt.setBytes _)
  def uuid(name: String, values: TraversableOnce[UUID]): Unit = list[Array[Byte]](name, values.map(uuidToByteArray(_)), stmt.setBytes _)
  def uuidOption(name: String, value: Option[UUID]): Unit = {
    value.map(uuid(name, _)).getOrElse(insert(name, Types.VARCHAR, stmt.setNull _))
  }

  def uuidToByteArray(uuid: UUID): Array[Byte] = {
    val bb = ByteBuffer.wrap(new Array[Byte](16))
    bb.putLong(uuid.getMostSignificantBits)
    bb.putLong(uuid.getLeastSignificantBits)
    bb.array()
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