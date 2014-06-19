package com.lucidchart.open.relate

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.sql.{Date => SqlDate, PreparedStatement, Statement, Timestamp, Types}
import java.math.{BigDecimal => JBigDecimal, BigInteger => JBigInt}
import java.io.ByteArrayInputStream
import java.util.{Date, UUID}
import java.nio.ByteBuffer
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable
import scala.io.Source
import scala.reflect.ClassTag

/**
 * A smart wrapper around the PreparedStatement class that allows inserting
 * parameter values by name rather than by index. Provides methods for inserting
 * all necessary datatypes.
 */
class SqlStatement(val stmt: PreparedStatement, val names: scala.collection.Map[String, List[Int]],
  val listParams: mutable.Map[String, ListParam]) {

  override protected def finalize() {
    // This is a failsafe to clean up the prepared statement
    //
    // It's possible that people unfamiliar with the API may do something like
    // SQL("insert into....").on(...)
    //
    // As of the time I'm writing this comment, that would leave a statement
    // open, and leak the resource.
    if (!stmt.isClosed()) {
      stmt.close()
    }
  }

  private def list[A](name: String, values: TraversableOnce[A], rule: (Int, A) => Unit) {
    val valueIterator = values.toIterator
    val nameData = names.get(name)

    if (nameData.isDefined) {
      var i = 0
      while (valueIterator.hasNext) {
        val value = valueIterator.next
        val parameterIterator = nameData.get.toIterator

        while (parameterIterator.hasNext) {
          rule(parameterIterator.next + i, value)
        }

        i += 1
      }
    }
  }

  private def insert[A](name: String, value: A, rule: (Int, A) => Unit) {
    val nameData = names.get(name)
    if (nameData.isDefined) {
      val iterator = nameData.get.toIterator
      while(iterator.hasNext) {
        rule(iterator.next, value)
      }
    }
  }

  /**
   * Set a BigDecimal in the PreparedStatement
   * @param name the name of the parameter to put the BigDecimal in
   * @param value the BigDecimal to put in the query
   */
  def bigDecimal(name: String, value: BigDecimal) = insert(name, value.bigDecimal, stmt.setBigDecimal _)
  def bigDecimals(name: String, values: TraversableOnce[BigDecimal]): Unit = list[JBigDecimal](name, values.map(_.bigDecimal), stmt.setBigDecimal _)

  /**
   * Set a Java BigDecimal in the PreparedStatement
   * @param name the name of the parameter to put the BigDecimal in 
   * @param value the BigDecimal to put in the query
   */
  def bigDecimal(name: String, value: JBigDecimal) = insert(name, value, stmt.setBigDecimal _)
  def bigDecimals[X: ClassTag](name: String, values: TraversableOnce[JBigDecimal]): Unit = list[JBigDecimal](name, values, stmt.setBigDecimal _)
  def bigDecimalOption[A](name: String, value: Option[A])(implicit bd: Query.BigDecimalLike[A]): Unit = {
    value.map(d => bigDecimal(name, bd.get(d))).getOrElse(insert(name, Types.DECIMAL, stmt.setNull _))
  }

  /**
   * Set a BigInt in the PreparedStatement
   * @param name the name of the parameter to put the BigInt in 
   * @param value the BigInt to put into the query
   */
  def bigInt(name: String, value: BigInt) = insert(name, new JBigDecimal(value.bigInteger), stmt.setBigDecimal _)
  def bigInts(name: String, values: TraversableOnce[BigInt]): Unit = list[JBigDecimal](name, values.map{ i: BigInt => new JBigDecimal(i.bigInteger) }, stmt.setBigDecimal _)

  /**
   * Set a Java BigInteger in the PreparedStatement
   * @param name the name of the parameter to put the BigInteger in
   * @param value the BigInteger to put in the query
   */
  def bigInt(name: String, value: JBigInt) = insert(name, new JBigDecimal(value), stmt.setBigDecimal _)
  def bigInts[X: ClassTag](name: String, values: TraversableOnce[JBigInt]): Unit = list[JBigDecimal](name, values.map(new JBigDecimal(_)), stmt.setBigDecimal _)
  def bigIntOption[A](name: String, value: Option[A])(implicit bd: Query.BigIntLike[A]): Unit = {
    value.map(i => bigInt(name, bd.get(i))).getOrElse(insert(name, Types.BIGINT, stmt.setNull _))
  }

  /**
   * Set a Boolean in the PreparedStatement
   * @param name the name of the parameter to put the Boolean in
   * @param value the Boolean to put in the query
   */
  def bool(name: String, value: Boolean) = insert(name, value, stmt.setBoolean _)
  def bools(name: String, values: TraversableOnce[Boolean]): Unit = list[Boolean](name, values, stmt.setBoolean _)
  def boolOption(name: String, value: Option[Boolean]): Unit = {
    value.map(bool(name, _)).getOrElse(insert(name, Types.BOOLEAN, stmt.setNull _))
  }

  /**
   * Set a Byte in the PreparedStatement
   * @param name the name of the parameter to put the Byte in
   * @param value the Byte to put in the query
   */
  def byte(name: String, value: Byte) = insert(name, value, stmt.setByte _)
  def bytes(name: String, values: TraversableOnce[Byte]): Unit = list[Byte](name, values, stmt.setByte _)
  def byteOption(name: String, value: Option[Byte]): Unit = {
    value.map(byte(name, _)).getOrElse(insert(name, Types.TINYINT, stmt.setNull _))
  }

  /**
   * Set a ByteArray in the PreparedStatement
   * @param name the name of the parameter to put the ArrayByte in
   * @param value the ByteArray to put in the query
   */
  def byteArray(name: String, value: Array[Byte]) = insert(name, value, stmt.setObject _)
  def byteArrays(name: String, values: TraversableOnce[Array[Byte]]) = list(name, values, stmt.setObject _)
  def byteArrayOption(name: String, value: Option[Array[Byte]]) = {
    value.map(byteArray(name, _)).getOrElse(insert(name, Types.BLOB, stmt.setNull _))
  }

  /**
   * Set a Char in the PreparedStatement
   * @param name the name of the parameter to put the Char in 
   * @param value the Char to put in the query
   */
  def char(name: String, value: Char) = insert(name, value.toString, stmt.setString _)
  def chars(name: String, values: TraversableOnce[Char]): Unit = list[String](name, values.map(_.toString), stmt.setString _)
  def charOption(name: String, value: Option[Char]): Unit = {
    value.map(char(name, _)).getOrElse(insert(name, Types.CHAR, stmt.setNull _))
  }

  /**
   * Set a Date in the PreparedStatement
   * @param name the name of the parameter to put the Date in
   * @param value the Date to put in the query
   */
  def date(name: String, value: Date) = insert(name, new Timestamp(value.getTime), stmt.setTimestamp _)
  def dates(name: String, values: TraversableOnce[Date]): Unit = list[Timestamp](name, values.map{ d: Date => new Timestamp(d.getTime)}, stmt.setTimestamp _)
  def dateOption(name: String, value: Option[Date]): Unit = {
    value.map(date(name, _)).getOrElse(insert(name, Types.DATE, stmt.setNull _))
  }

  /**
   * Set a Double in the PreparedStatement
   * @param name the name of the parameter to put the Double in
   * @param value the Double to put in the query
   */
  def double(name: String, value: Double) = insert(name, value, stmt.setDouble _)
  def doubles(name: String, values: TraversableOnce[Double]): Unit = list[Double](name, values, stmt.setDouble _)
  def doubleOption(name: String, value: Option[Double]): Unit = {
    value.map(double(name, _)).getOrElse(insert(name, Types.DOUBLE, stmt.setNull _))
  }

  /**
   * Set a Float in the PreparedStatement
   * @param name the name of the parameter to put the Float in
   * @param value the Float to put in the query
   */
  def float(name: String, value: Float) = insert(name, value, stmt.setFloat _)
  def floats(name: String, values: TraversableOnce[Float]): Unit = list[Float](name, values, stmt.setFloat _)
  def floatOption(name: String, value: Option[Float]): Unit = {
    value.map(float(name, _)).getOrElse(insert(name, Types.FLOAT, stmt.setNull _))
  }

  /**
   * Set an Int in the PreparedStatement
   * @param name the name of the parameter to put the int in
   * @param value the int to put in the query
   */
  def int(name: String, value: Int) = insert(name, value, stmt.setInt _)
  def ints(name: String, values: TraversableOnce[Int]): Unit = list[Int](name, values, stmt.setInt _)
  def intOption(name: String, value: Option[Int]): Unit = {
    value.map(int(name, _)).getOrElse(insert(name, Types.INTEGER, stmt.setNull _))
  }

  /**
   * Set a Long in the PreparedStatement
   * @param name the name of the parameter to put the Long in
   * @param value the Long to put in the query
   */
  def long(name: String, value: Long) = insert(name, value, stmt.setLong)
  def longs(name: String, values: TraversableOnce[Long]): Unit = list[Long](name, values, stmt.setLong _)
  def longOption(name: String, value: Option[Long]): Unit = {
    value.map(long(name, _)).getOrElse(insert(name, Types.BIGINT, stmt.setNull _))
  }

  /**
   * Set a Short in the PreparedStatement
   * @param name the name of the parameter to put the Short in
   * @param value the Short to put in the query
   */
  def short(name: String, value: Short) = insert(name, value, stmt.setShort _)
  def shorts(name: String, values: TraversableOnce[Short]): Unit = list[Short](name, values, stmt.setShort _)
  def shortOption(name: String, value: Option[Short]): Unit = {
    value.map(short(name, _)).getOrElse(insert(name, Types.SMALLINT, stmt.setNull _))
  }

  /**
   * Set a String in the PreparedStatement
   * @param name the name of the parameter to put the string in
   * @param value the value to put in the query
   */
  def string(name: String, value: String) = insert(name, value, stmt.setString _)
  def strings(name: String, values: TraversableOnce[String]): Unit = list[String](name, values, stmt.setString _)
  def stringOption(name: String, value: Option[String]): Unit = {
    value.map(string(name, _)).getOrElse(insert(name, Types.VARCHAR, stmt.setNull _))
  }

  /**
   * Set a Timestamp in the PreparedStatement
   * @param name the name of the parameter to put the Timestamp in
   * @param value the Timestamp to put into the query
   */
  def timestamp(name: String, value: Timestamp) = insert(name, value, stmt.setTimestamp)
  def timestamps(name: String, values: TraversableOnce[Timestamp]): Unit = list[Timestamp](name, values, stmt.setTimestamp _)
  def timestampOption(name: String, value: Option[Timestamp]): Unit = {
    value.map(timestamp(name, _)).getOrElse(insert(name, Types.TIMESTAMP, stmt.setNull _))
  }

  /**
   * Set a UUID in the PreparedStatement
   * @param name the name of the parameter to put the UUID in
   * @param value the UUID to put in the query
   */
  def uuid(name: String, value: UUID) = insert(name, ByteHelper.uuidToByteArray(value), stmt.setBytes _)
  def uuids(name: String, values: TraversableOnce[UUID]): Unit = list[Array[Byte]](name, values.map(ByteHelper.uuidToByteArray(_)), stmt.setBytes _)
  def uuidOption(name: String, value: Option[UUID]): Unit = {
    value.map(uuid(name, _)).getOrElse(insert(name, Types.VARCHAR, stmt.setNull _))
  }

  /**
   * Execute a statement
   */
  def execute(): Boolean = {
    try {
      stmt.execute()
    }
    finally {
      stmt.close()
    }
  }

  /**
   * Execute an update
   */
  def executeUpdate(): Int = {
    try {
      stmt.executeUpdate()
    }
    finally {
      stmt.close()
    }
  }

  /**
   * Execute a query
   */
  @deprecated("Use asList, asMap, or one of the other as* functions instead. This executeQuery function may leak connections", "1.1")
  def executeQuery(): SqlResult = {
    SqlResult(stmt.executeQuery())
  }

  /**
   * Execute an insert
   */
  @deprecated("Use executeInsertLong, executeInsertSingle, or one of the other executeInsert* functions instead. This executeInsert function may leak connections.", "1.1")
  def executeInsert(): SqlResult = {
    stmt.executeUpdate()
    SqlResult(stmt.getGeneratedKeys())
  }

  def executeInsertInt(): Int = withExecutedResults(true)(_.asSingle(RowParser.insertInt))
  def executeInsertInts(): List[Int] = withExecutedResults(true)(_.asList(RowParser.insertInt))
  def executeInsertLong(): Long = withExecutedResults(true)(_.asSingle(RowParser.insertLong))
  def executeInsertLongs(): List[Long] = withExecutedResults(true)(_.asList(RowParser.insertLong))

  def executeInsertSingle[U](parser: RowParser[U]): U = withExecutedResults(true)(_.asSingle(parser))
  def executeInsertCollection[U, T[_]](parser: RowParser[U])(implicit cbf: CanBuildFrom[T[U], U, T[U]]): T[U] = withExecutedResults(true)(_.asCollection(parser))

  protected def withExecutedResults[A](insert: Boolean)(callback: (SqlResult) => A): A = {
    try {
      val resultSet = if (insert) {
        stmt.executeUpdate()
        stmt.getGeneratedKeys()
      }
      else {
        stmt.executeQuery()
      }

      try {
        callback(SqlResult(resultSet))
      }
      finally {
        resultSet.close()
      }
    }
    finally {
      stmt.close()
    }
  }

  def asSingle[A](parser: RowParser[A]): A = withExecutedResults(false)(_.asSingle(parser))
  def asSingleOption[A](parser: RowParser[A]): Option[A] = withExecutedResults(false)(_.asSingleOption(parser))
  def asSet[A](parser: RowParser[A]): Set[A] = withExecutedResults(false)(_.asSet(parser))
  def asSeq[A](parser: RowParser[A]): Seq[A] = withExecutedResults(false)(_.asSeq(parser))
  def asIterable[A](parser: RowParser[A]): Iterable[A] = withExecutedResults(false)(_.asIterable(parser))
  def asList[A](parser: RowParser[A]): List[A] = withExecutedResults(false)(_.asList(parser))
  def asMap[U, V](parser: RowParser[(U, V)]): Map[U, V] = withExecutedResults(false)(_.asMap(parser))
  def asScalar[A](): A = withExecutedResults(false)(_.asScalar[A]())
  def asScalarOption[A](): Option[A] = withExecutedResults(false)(_.asScalarOption[A]())
  def asCollection[U, T[_]](parser: RowParser[U])(implicit cbf: CanBuildFrom[T[U], U, T[U]]): T[U] = withExecutedResults(false)(_.asCollection(parser))
  def asPairCollection[U, V, T[_, _]](parser: RowParser[(U, V)])(implicit cbf: CanBuildFrom[T[U, V], (U, V), T[U, V]]): T[U, V] = withExecutedResults(false)(_.asPairCollection(parser))

}

/** This class is used to insert tuple data into a prepared statement */
private[relate] case class TupleStatement(
  stmt: PreparedStatement,
  params: Map[String, Int],
  index: Int // the index in the prepared statement that this tuple starts at
) {
  
  /** 
   * Insert a value into the PreparedStatement
   * @param name the identifier for the value
   * @param value the value to insert
   * @param rule the rule to use on the PreparedStatement
   */
  private def insert[A](name: String, value: A, rule: (Int, A) => Unit) {
    rule(index + params(name), value)
  }

  def bigDecimal(name: String, value: BigDecimal) = insert(name, value.bigDecimal, stmt.setBigDecimal _)
  def bigDecimalOption(name: String, value: Option[BigDecimal]) = value.map(bigDecimal(name, _)).getOrElse(insert(name, Types.DECIMAL, stmt.setNull _))
  def bigDecimal(name: String, value: JBigDecimal) = insert(name, value, stmt.setBigDecimal _)
  def bigDecimalOption[X: ClassTag](name: String, value: Option[JBigDecimal]) = value.map(bigDecimal(name, _)).getOrElse(insert(name, Types.DECIMAL, stmt.setNull _))
  def bigInt(name: String, value: BigInt) = insert(name, new JBigDecimal(value.bigInteger), stmt.setBigDecimal _)
  def bigIntOption(name: String, value: Option[BigInt]) = value.map(bigInt(name, _)).getOrElse(insert(name, Types.DECIMAL, stmt.setNull _))
  def bigInt(name: String, value: JBigInt) = insert(name, new JBigDecimal(value), stmt.setBigDecimal _)
  def bigIntOption[X: ClassTag](name: String, value: Option[JBigInt]) = value.map(bigInt(name, _)).getOrElse(insert(name, Types.DECIMAL, stmt.setNull _))
  def bool(name: String, value: Boolean) = insert(name, value, stmt.setBoolean _)
  def boolOption(name: String, value: Option[Boolean]) = value.map(bool(name, _)).getOrElse(insert(name, Types.BOOLEAN, stmt.setNull _))
  def byte(name: String, value: Byte) = insert(name, value, stmt.setByte _)
  def byteOption(name: String, value: Option[Byte]) = value.map(byte(name, _)).getOrElse(insert(name, Types.TINYINT, stmt.setNull _))
  def char(name: String, value: Char) = insert(name, value.toString, stmt.setString _)
  def charOption(name: String, value: Option[Char]) = value.map(char(name, _)).getOrElse(insert(name, Types.CHAR, stmt.setNull _))
  def date(name: String, value: Date) = insert(name, new SqlDate(value.getTime), stmt.setDate _)
  def dateOption(name: String, value: Option[Date]) = value.map(date(name, _)).getOrElse(insert(name, Types.DATE, stmt.setNull _))
  def double(name: String, value: Double) = insert(name, value, stmt.setDouble _)
  def doubleOption(name: String, value: Option[Double]) = value.map(double(name, _)).getOrElse(insert(name, Types.DOUBLE, stmt.setNull _))
  def float(name: String, value: Float) = insert(name, value, stmt.setFloat _)
  def floatOption(name: String, value: Option[Float]) = value.map(float(name, _)).getOrElse(insert(name, Types.FLOAT, stmt.setNull _))
  def int(name: String, value: Int) = insert(name, value, stmt.setInt _)
  def intOption(name: String, value: Option[Int]) = value.map(int(name, _)).getOrElse(insert(name, Types.INTEGER, stmt.setNull _))
  def long(name: String, value: Long) = insert(name, value, stmt.setLong _)
  def longOption(name: String, value: Option[Long]) = value.map(long(name, _)).getOrElse(insert(name, Types.BIGINT, stmt.setNull _))
  def short(name: String, value: Short) = insert(name, value, stmt.setShort _)
  def shortOption(name: String, value: Option[Short]) = value.map(short(name, _)).getOrElse(insert(name, Types.SMALLINT, stmt.setNull _))
  def string(name: String, value: String) = insert(name, value, stmt.setString _)
  def stringOption(name: String, value: Option[String]) = value.map(string(name, _)).getOrElse(insert(name, Types.VARCHAR, stmt.setNull _))
  def timestamp(name: String, value: Timestamp) = insert(name, value, stmt.setTimestamp _)
  def timestampOption(name: String, value: Option[Timestamp]) = value.map(timestamp(name, _)).getOrElse(insert(name, Types.TIMESTAMP, stmt.setNull _))
  def uuid(name: String, value: UUID) = insert(name, ByteHelper.uuidToByteArray(value), stmt.setBytes _)
  def uuidOption(name: String, value: Option[UUID]) = value.map(uuid(name, _)).getOrElse(insert(name, Types.VARCHAR, stmt.setNull _))
}

object ByteHelper {

  def uuidToByteArray(uuid: UUID): Array[Byte] = {
    val bb = ByteBuffer.wrap(new Array[Byte](16))
    bb.putLong(uuid.getMostSignificantBits)
    bb.putLong(uuid.getLeastSignificantBits)
    bb.array()
  }

}
