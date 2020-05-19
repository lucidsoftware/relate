package com.lucidchart.relate

import java.sql.PreparedStatement
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
  implicit def single[A : Parameterizable](value: A): SingleParameter = new SingleParameter {
    protected[this] def set(statement: PreparedStatement, i: Int) =
      implicitly[Parameterizable[A]].set(statement, i, value)
  }
  implicit def singleOption[A : Parameterizable](value: Option[A]): SingleParameter = new SingleParameter {
    protected[this] def set(statement: PreparedStatement, i: Int) =
      implicitly[Parameterizable[A]].setOption(statement, i, value)
  }

  implicit def fromByteArray(it: Array[Byte]) = single(it)
  implicit def fromArray[A <% SingleParameter](it: Array[A]) = new TupleParameter(it.map(elem => elem: SingleParameter))
  implicit def fromIterable[A <% SingleParameter](it: Iterable[A]) = new TupleParameter(it.map(elem => elem: SingleParameter))
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

  implicit def fromTuples[A <% TupleParameter](seq: Seq[A]) = new TuplesParameter(seq.map(tuple => tuple: TupleParameter))
}

trait SingleParameter extends Parameter {
  protected[this] def set(statement: PreparedStatement, i: Int)

  def appendPlaceholders(stringBuilder: StringBuilder) = stringBuilder.append("?")
  def parameterize(statement: PreparedStatement, i: Int) = {
    set(statement, i)
    i + 1
  }
}

trait MultipleParameter extends Parameter {
  protected def params: Iterable[Parameter]
  def parameterize(statement: PreparedStatement, i: Int) = {
    params.foldLeft(i) { (i, param) =>
      param.parameterize(statement, i)
    }
  }
}

class TupleParameter(val params: Iterable[SingleParameter]) extends MultipleParameter {
  def appendPlaceholders(stringBuilder: StringBuilder) = {
    val length = params.size
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

class TuplesParameter(val params: Iterable[TupleParameter]) extends MultipleParameter {
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
