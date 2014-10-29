package com.lucidchart.open.relate.interp

import com.lucidchart.open.relate.ByteHelper
import java.net.URL
import java.sql.{Blob, Clob, Date, NClob, PreparedStatement, Ref, RowId, SQLXML, Timestamp, Time, Types}
import java.util.UUID
import scala.language.implicitConversions

/*
 * Does not support
 *   - setAsciiStream(InputStream)
 *   - setAsciiStream(InputStream, int)
 *   - setAsciiStream(InputStream, long)
 *   - setBinaryStream(InputStream)
 *   - setBinaryStream(InputStream, int)
 *   - setBinaryStream(InputStream, long)
 *   - setBlob(InputStream)
 *   - setBlob(InputStream. long)
 *   - setCharacterStream(Reader)
 *   - setCharacterStream(Reader, int)
 *   - setCharacterStream(Reader, long)
 *   - setClob(Reader)
 *   - setClob(Reader, long)
 *   - setDate(Date, Calendar)
 *   - setNCharacterStream(Reader)
 *   - setNCharacterStream(Reader, long)
 *   - setNClob(Reader)
 *   - setNClob(Reader, long)
 *   - setNString(String)
 *   - setObject(Object
 *   - setObject(Object, int)
 *   - setObject(Object, int, int)
 *   - setTimestamp(Timestamp, Calendar)
 */

trait Parameter {
  def appendPlaceholders(stringBuilder: StringBuilder)
  def parameterize(statement: PreparedStatement, i: Int): Int
}

object Parameter {
  implicit def fromArray(value: java.sql.Array) = new ArrayParameter(value)
  implicit def fromBigDecimal(value: java.math.BigDecimal) = new BigDecimalParameter(value)
  implicit def fromBlob(value: Blob) = new BlobParameter(value)
  implicit def fromByte(value: Byte) = new ByteParameter(value)
  implicit def fromByteArray(value: Array[Byte]) = new ByteArrayParameter(value)
  implicit def fromClob(value: Clob) = new ClobParameter(value)
  implicit def fromDate(value: Date) = new DateParameter(value)
  implicit def fromDouble(value: Double) = new DoubleParameter(value)
  implicit def fromFloat(value: Float) = new FloatParameter(value)
  implicit def fromInt(value: Int) = new IntParameter(value)
  implicit def fromLong(value: Long) = new LongParameter(value)
  implicit def fromNClob(value: NClob) = new NClobParameter(value)
  implicit def fromRef(value: Ref) = new RefParameter(value)
  implicit def fromRowId(value: RowId) = new RowIdParameter(value)
  implicit def fromShort(value: Short) = new ShortParameter(value)
  implicit def fromSqlXml(value: SQLXML) = new SqlXmlParameter(value)
  implicit def fromString(value: String) = new StringParameter(value)
  implicit def fromTime(value: Time) = new TimeParameter(value)
  implicit def fromTimestamp(value: Timestamp) = new TimestampParameter(value)
  implicit def fromUrl(value: URL) = new UrlParameter(value)
  implicit def fromOptionalArray(value: Option[java.sql.Array]) = value.map(fromArray).getOrElse(NullArrayParameter)
  implicit def fromOptionalBigDecimal(value: Option[java.math.BigDecimal]) = value.map(fromBigDecimal).getOrElse(NullNumericParameter)
  implicit def fromOptionalBlob(value: Option[Blob]) = value.map(fromBlob).getOrElse(NullBlobParameter)
  implicit def fromOptionalByte(value: Option[Byte]) = value.map(fromByte).getOrElse(NullTinyIntParameter)
  implicit def fromOptionalByteArray(value: Option[Array[Byte]]) = value.map(fromByteArray).getOrElse(NullVarBinaryParameter)
  implicit def fromOptionalClob(value: Option[Clob]) = value.map(fromClob).getOrElse(NullClobParameter)
  implicit def fromOptionalDate(value: Option[Date]) = value.map(fromDate).getOrElse(NullDateParameter)
  implicit def fromOptionalDouble(value: Option[Double]) = value.map(fromDouble).getOrElse(NullDoubleParameter)
  implicit def fromOptionalFloat(value: Option[Float]) = value.map(fromFloat).getOrElse(NullFloatParameter)
  implicit def fromOptionalInt(value: Option[Int]) = value.map(fromInt).getOrElse(NullIntegerParameter)
  implicit def fromOptionalLong(value: Option[Long]) = value.map(fromLong).getOrElse(NullBigIntParameter)
  implicit def fromOptionalNClob(value: Option[NClob]) = value.map(fromNClob).getOrElse(NullNClobParameter)
  implicit def fromOptionalRef(value: Option[Ref]) = value.map(fromRef).getOrElse(NullRefParameter)
  implicit def fromOptionalRowId(value: Option[RowId]) = value.map(fromRowId).getOrElse(NullRowIdParameter)
  implicit def fromOptionalShort(value: Option[Short]) = value.map(fromShort).getOrElse(NullSmallIntParameter)
  implicit def fromOptionalSqlXml(value: Option[SQLXML]) = value.map(fromSqlXml).getOrElse(NullSqlXmlParameter)
  implicit def fromOptionalString(value: Option[String]) = value.map(fromString).getOrElse(NullVarCharParameter)
  implicit def fromOptionalTime(value: Option[Time]) = value.map(fromTime).getOrElse(NullTimeParameter)
  implicit def fromOptionalTimestamp(value: Option[Timestamp]) = value.map(fromTimestamp).getOrElse(NullTimestampParameter)
  implicit def fromOptionalUrl(value: Option[URL]) = value.map(fromUrl).getOrElse(NullDatalinkParameter)

  implicit def fromJavaDate(value: java.util.Date) = fromTimestamp(new Timestamp(value.getTime))
  implicit def fromUuid(value: UUID) = fromByteArray(ByteHelper.uuidToByteArray(value))
  implicit def fromOptionalJavaDate(value: Option[java.util.Date]) = value.map(fromJavaDate).getOrElse(NullTimestampParameter)
  implicit def fromOptionalUuid(value: Option[UUID]) = value.map(fromUuid).getOrElse(NullVarBinaryParameter)

  implicit def fromSeq[A <% SingleParameter](seq: Seq[A]) = new TupleParameter(seq.map(elem => elem: SingleParameter))
  type SP = SingleParameter
  implicit def fromTuple1[T1 <% SP](t: Tuple1[T1]) = TupleParameter(t._1)
  implicit def fromTuple2[T1 <% SP,T2 <% SP](t: Tuple2[T1,T2]) = TupleParameter(t._1, t._2)
  implicit def fromTuple3[T1 <% SP,T2 <% SP,T3 <% SP](t: Tuple3[T1,T2,T3]) = TupleParameter(t._1, t._2, t._3)
  implicit def fromTuple4[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP](t: Tuple4[T1,T2,T3,T4]) = TupleParameter(t._1, t._2, t._3, t._4)
  implicit def fromTuple5[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP](t: Tuple5[T1,T2,T3,T4,T5]) = TupleParameter(t._1, t._2, t._3, t._4, t._5)
  implicit def fromTuple6[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP](t: Tuple6[T1,T2,T3,T4,T5,T6]) = TupleParameter(t._1, t._2, t._3, t._4, t._5, t._6)
  implicit def fromTuple7[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP](t: Tuple7[T1,T2,T3,T4,T5,T6,T7]) = TupleParameter(t._1, t._2, t._3, t._4, t._5, t._6, t._7)
  implicit def fromTuple8[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP](t: Tuple8[T1,T2,T3,T4,T5,T6,T7,T8]) = TupleParameter(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8)
  implicit def fromTuple9[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP](t: Tuple9[T1,T2,T3,T4,T5,T6,T7,T8,T9]) = TupleParameter(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9)
  implicit def fromTuple10[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP](t: Tuple10[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10]) = TupleParameter(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10)
  implicit def fromTuple11[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP](t: Tuple11[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11]) = TupleParameter(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11)
  implicit def fromTuple12[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP,T12 <% SP](t: Tuple12[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12]) = TupleParameter(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12)
  implicit def fromTuple13[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP,T12 <% SP,T13 <% SP](t: Tuple13[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13]) = TupleParameter(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13)
  implicit def fromTuple14[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP,T12 <% SP,T13 <% SP,T14 <% SP](t: Tuple14[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14]) = TupleParameter(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14)
  implicit def fromTuple15[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP,T12 <% SP,T13 <% SP,T14 <% SP,T15 <% SP](t: Tuple15[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14,T15]) = TupleParameter(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15)
  implicit def fromTuple16[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP,T12 <% SP,T13 <% SP,T14 <% SP,T15 <% SP,T16 <% SP](t: Tuple16[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14,T15,T16]) = TupleParameter(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15, t._16)
  implicit def fromTuple17[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP,T12 <% SP,T13 <% SP,T14 <% SP,T15 <% SP,T16 <% SP,T17 <% SP](t: Tuple17[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14,T15,T16,T17]) = TupleParameter(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15, t._16, t._17)
  implicit def fromTuple18[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP,T12 <% SP,T13 <% SP,T14 <% SP,T15 <% SP,T16 <% SP,T17 <% SP,T18 <% SP](t: Tuple18[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14,T15,T16,T17,T18]) = TupleParameter(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15, t._16, t._17, t._18)
  implicit def fromTuple19[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP,T12 <% SP,T13 <% SP,T14 <% SP,T15 <% SP,T16 <% SP,T17 <% SP,T18 <% SP,T19 <% SP](t: Tuple19[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14,T15,T16,T17,T18,T19]) = TupleParameter(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15, t._16, t._17, t._18, t._19)
  implicit def fromTuple20[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP,T12 <% SP,T13 <% SP,T14 <% SP,T15 <% SP,T16 <% SP,T17 <% SP,T18 <% SP,T19 <% SP,T20 <% SP](t: Tuple20[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14,T15,T16,T17,T18,T19,T20]) = TupleParameter(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15, t._16, t._17, t._18, t._19, t._20)
  implicit def fromTuple21[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP,T12 <% SP,T13 <% SP,T14 <% SP,T15 <% SP,T16 <% SP,T17 <% SP,T18 <% SP,T19 <% SP,T20 <% SP,T21 <% SP](t: Tuple21[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14,T15,T16,T17,T18,T19,T20,T21]) = TupleParameter(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15, t._16, t._17, t._18, t._19, t._20, t._21)
  implicit def fromTuple22[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP,T12 <% SP,T13 <% SP,T14 <% SP,T15 <% SP,T16 <% SP,T17 <% SP,T18 <% SP,T19 <% SP,T20 <% SP,T21 <% SP,T22 <% SP](t: Tuple22[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14,T15,T16,T17,T18,T19,T20,T21,T22]) = TupleParameter(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15, t._16, t._17, t._18, t._19, t._20, t._21, t._22)

  // can't combine into [A <% TupleParameter](seq: Seq[A]) until Scala 2.11.x; see https://issues.scala-lang.org/browse/SI-3346
  implicit def fromTupleParameters(seq: Seq[TupleParameter]) = new TuplesParameter(seq)
  implicit def fromSeqTuple1[T1 <% SP](seq: Seq[Tuple1[T1]]): TuplesParameter = seq.map(elem => elem: TupleParameter)
  implicit def fromSeqTuple2[T1 <% SP,T2 <% SP](seq: Seq[Tuple2[T1,T2]]): TuplesParameter = seq.map(elem => elem: TupleParameter)
  implicit def fromSeqTuple3[T1 <% SP,T2 <% SP,T3 <% SP](seq: Seq[Tuple3[T1,T2,T3]]): TuplesParameter = seq.map(elem => elem: TupleParameter)
  implicit def fromSeqTuple4[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP](seq: Seq[Tuple4[T1,T2,T3,T4]]): TuplesParameter = seq.map(elem => elem: TupleParameter)
  implicit def fromSeqTuple5[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP](seq: Seq[Tuple5[T1,T2,T3,T4,T5]]): TuplesParameter = seq.map(elem => elem: TupleParameter)
  implicit def fromSeqTuple6[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP](seq: Seq[Tuple6[T1,T2,T3,T4,T5,T6]]): TuplesParameter = seq.map(elem => elem: TupleParameter)
  implicit def fromSeqTuple7[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP](seq: Seq[Tuple7[T1,T2,T3,T4,T5,T6,T7]]): TuplesParameter = seq.map(elem => elem: TupleParameter)
  implicit def fromSeqTuple8[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP](seq: Seq[Tuple8[T1,T2,T3,T4,T5,T6,T7,T8]]): TuplesParameter = seq.map(elem => elem: TupleParameter)
  implicit def fromSeqTuple9[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP](seq: Seq[Tuple9[T1,T2,T3,T4,T5,T6,T7,T8,T9]]): TuplesParameter = seq.map(elem => elem: TupleParameter)
  implicit def fromSeqTuple10[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP](seq: Seq[Tuple10[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10]]): TuplesParameter = seq.map(elem => elem: TupleParameter)
  implicit def fromSeqTuple11[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP](seq: Seq[Tuple11[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11]]): TuplesParameter = seq.map(elem => elem: TupleParameter)
  implicit def fromSeqTuple12[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP,T12 <% SP](seq: Seq[Tuple12[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12]]): TuplesParameter = seq.map(elem => elem: TupleParameter)
  implicit def fromSeqTuple13[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP,T12 <% SP,T13 <% SP](seq: Seq[Tuple13[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13]]): TuplesParameter = seq.map(elem => elem: TupleParameter)
  implicit def fromSeqTuple14[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP,T12 <% SP,T13 <% SP,T14 <% SP](seq: Seq[Tuple14[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14]]): TuplesParameter = seq.map(elem => elem: TupleParameter)
  implicit def fromSeqTuple15[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP,T12 <% SP,T13 <% SP,T14 <% SP,T15 <% SP](seq: Seq[Tuple15[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14,T15]]): TuplesParameter = seq.map(elem => elem: TupleParameter)
  implicit def fromSeqTuple16[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP,T12 <% SP,T13 <% SP,T14 <% SP,T15 <% SP,T16 <% SP](seq: Seq[Tuple16[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14,T15,T16]]): TuplesParameter = seq.map(elem => elem: TupleParameter)
  implicit def fromSeqTuple17[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP,T12 <% SP,T13 <% SP,T14 <% SP,T15 <% SP,T16 <% SP,T17 <% SP](seq: Seq[Tuple17[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14,T15,T16,T17]]): TuplesParameter = seq.map(elem => elem: TupleParameter)
  implicit def fromSeqTuple18[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP,T12 <% SP,T13 <% SP,T14 <% SP,T15 <% SP,T16 <% SP,T17 <% SP,T18 <% SP](seq: Seq[Tuple18[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14,T15,T16,T17,T18]]): TuplesParameter = seq.map(elem => elem: TupleParameter)
  implicit def fromSeqTuple19[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP,T12 <% SP,T13 <% SP,T14 <% SP,T15 <% SP,T16 <% SP,T17 <% SP,T18 <% SP,T19 <% SP](seq: Seq[Tuple19[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14,T15,T16,T17,T18,T19]]): TuplesParameter = seq.map(elem => elem: TupleParameter)
  implicit def fromSeqTuple20[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP,T12 <% SP,T13 <% SP,T14 <% SP,T15 <% SP,T16 <% SP,T17 <% SP,T18 <% SP,T19 <% SP,T20 <% SP](seq: Seq[Tuple20[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14,T15,T16,T17,T18,T19,T20]]): TuplesParameter = seq.map(elem => elem: TupleParameter)
  implicit def fromSeqTuple21[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP,T12 <% SP,T13 <% SP,T14 <% SP,T15 <% SP,T16 <% SP,T17 <% SP,T18 <% SP,T19 <% SP,T20 <% SP,T21 <% SP](seq: Seq[Tuple21[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14,T15,T16,T17,T18,T19,T20,T21]]): TuplesParameter = seq.map(elem => elem: TupleParameter)
  implicit def fromSeqTuple22[T1 <% SP,T2 <% SP,T3 <% SP,T4 <% SP,T5 <% SP,T6 <% SP,T7 <% SP,T8 <% SP,T9 <% SP,T10 <% SP,T11 <% SP,T12 <% SP,T13 <% SP,T14 <% SP,T15 <% SP,T16 <% SP,T17 <% SP,T18 <% SP,T19 <% SP,T20 <% SP,T21 <% SP,T22 <% SP](seq: Seq[Tuple22[T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14,T15,T16,T17,T18,T19,T20,T21,T22]]): TuplesParameter = seq.map(elem => elem: TupleParameter)
  implicit def fromSeqSeq[A <% SingleParameter](seq: Seq[Seq[A]]): TuplesParameter = seq.map(elem => elem: TupleParameter)

}

trait SingleParameter extends Parameter {
  protected def set(statement: PreparedStatement, i: Int)

  def appendPlaceholders(stringBuilder: StringBuilder) = stringBuilder.append("?")
  def parameterize(statement: PreparedStatement, i: Int) = {
    set(statement, i)
    i + 1
  }
}

trait MultipleParameter extends Parameter {
  protected def params: Seq[Parameter]
  def parameterize(statement: PreparedStatement, i: Int) = {
    params.foldLeft(i) { (i, param) =>
      param.parameterize(statement, i)
    }
  }
}

class TupleParameter(val params: Seq[SingleParameter]) extends MultipleParameter {
  def appendPlaceholders(stringBuilder: StringBuilder) = {
    val length = params.length
    if(length > 0) {
      stringBuilder.append("?")
      var i = 1
      while (i < length) {
        stringBuilder.append(",?")
        i += 1
      }
    }
  }
}

object TupleParameter {
  def apply(params: SingleParameter*) = new TupleParameter(params)
}

class TuplesParameter(val params: Seq[TupleParameter]) extends MultipleParameter {
  def appendPlaceholders(stringBuilder: StringBuilder) = {
    if(params.nonEmpty) {
      params.foreach { param =>
        stringBuilder.append("(")
        param.appendPlaceholders(stringBuilder)
        stringBuilder.append("),")
      }
      stringBuilder.length -= 1
    }
  }
}

class ArrayParameter(value: java.sql.Array) extends SingleParameter {
  protected def set(statement: PreparedStatement, i: Int) = statement.setArray(i, value)
}

class BigDecimalParameter(value: java.math.BigDecimal) extends SingleParameter {
  protected def set(statement: PreparedStatement, i: Int) = statement.setBigDecimal(i, value)
}

class BlobParameter(value: Blob) extends SingleParameter {
  protected def set(statement: PreparedStatement, i: Int) = statement.setBlob(i, value)
}

class BooleanParameter(value: Boolean) extends SingleParameter {
  protected def set(statement: PreparedStatement, i: Int) = statement.setBoolean(i, value)
}

class ByteParameter(value: Byte) extends SingleParameter {
  protected def set(statement: PreparedStatement, i: Int) = statement.setByte(i, value)
}

class ByteArrayParameter(value: Array[Byte]) extends SingleParameter {
  protected def set(statement: PreparedStatement, i: Int) = statement.setBytes(i, value)
}

class ClobParameter(value: Clob) extends SingleParameter {
  protected def set(statement: PreparedStatement, i: Int) = statement.setClob(i, value)
}

class DateParameter(value: Date) extends SingleParameter {
  protected def set(statement: PreparedStatement, i: Int) = statement.setDate(i, value)
}

class DoubleParameter(value: Double) extends SingleParameter {
  protected def set(statement: PreparedStatement, i: Int) = statement.setDouble(i, value)
}

class FloatParameter(value: Float) extends SingleParameter {
  protected def set(statement: PreparedStatement, i: Int) = statement.setFloat(i, value)
}

class IntParameter(value: Int) extends SingleParameter {
  protected def set(statement: PreparedStatement, i: Int) = statement.setInt(i, value)
}

class LongParameter(value: Long) extends SingleParameter {
  protected def set(statement: PreparedStatement, i: Int) = statement.setLong(i, value)
}

class NClobParameter(value: NClob) extends SingleParameter {
  protected def set(statement: PreparedStatement, i: Int) = statement.setNClob(i, value)
}

class RefParameter(value: Ref) extends SingleParameter {
  protected def set(statement: PreparedStatement, i: Int) = statement.setRef(i, value)
}

class RowIdParameter(value: RowId) extends SingleParameter {
  protected def set(statement: PreparedStatement, i: Int) = statement.setRowId(i, value)
}

class ShortParameter(value: Short) extends SingleParameter {
  protected def set(statement: PreparedStatement, i: Int) = statement.setShort(i, value)
}

class SqlXmlParameter(value: SQLXML) extends SingleParameter {
  protected def set(statement: PreparedStatement, i: Int) = statement.setSQLXML(i, value)
}

class StringParameter(value: String) extends SingleParameter {
  protected def set(statement: PreparedStatement, i: Int) = statement.setString(i, value)
}

class TimeParameter(value: Time) extends SingleParameter {
  protected def set(statement: PreparedStatement, i: Int) = statement.setTime(i, value)
}

class TimestampParameter(value: Timestamp) extends SingleParameter {
  protected def set(statement: PreparedStatement, i: Int) = statement.setTimestamp(i, value)
}

class UrlParameter(value: URL) extends SingleParameter {
  protected def set(statement: PreparedStatement, i: Int) = statement.setURL(i, value)
}

class NullParameter(`type`: Int) extends SingleParameter {
  protected def set(statement: PreparedStatement, i: Int) = statement.setNull(i, `type`)
}

object NullArrayParameter extends NullParameter(Types.ARRAY)

object NullBooleanParameter extends NullParameter(Types.BOOLEAN)

object NullBigIntParameter extends NullParameter(Types.BIGINT)

object NullBlobParameter extends NullParameter(Types.BLOB)

object NullClobParameter extends NullParameter(Types.CLOB)

object NullDatalinkParameter extends NullParameter(Types.DATALINK)

object NullDateParameter extends NullParameter(Types.DATE)

object NullDoubleParameter extends NullParameter(Types.DOUBLE)

object NullFloatParameter extends NullParameter(Types.FLOAT)

object NullIntegerParameter extends NullParameter(Types.INTEGER)

object NullNClobParameter extends NullParameter(Types.NCLOB)

object NullNumericParameter extends NullParameter(Types.NUMERIC)

object NullRefParameter extends NullParameter(Types.REF)

object NullRowIdParameter extends NullParameter(Types.ROWID)

object NullSmallIntParameter extends NullParameter(Types.SMALLINT)

object NullSqlXmlParameter extends NullParameter(Types.SQLXML)

object NullTimeParameter extends NullParameter(Types.TIME)

object NullTimestampParameter extends NullParameter(Types.TIMESTAMP)

object NullTinyIntParameter extends NullParameter(Types.TINYINT)

object NullVarBinaryParameter extends NullParameter(Types.VARBINARY)

object NullVarCharParameter extends NullParameter(Types.VARCHAR)
