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
  def placeholder: String
  def parameterize(statement: PreparedStatement, i: Int): Int
}

object Parameter {
  implicit def single[A: Parameterizable](value: A): SingleParameter = new SingleParameter {
    protected[this] def set(statement: PreparedStatement, i: Int) =
      implicitly[Parameterizable[A]].set(statement, i, value)
  }
  implicit def singleOption[A: Parameterizable](value: Option[A]): SingleParameter = new SingleParameter {
    protected[this] def set(statement: PreparedStatement, i: Int) =
      implicitly[Parameterizable[A]].setOption(statement, i, value)
  }

  type SP[T] = T => SingleParameter
  implicit def fromByteArray(it: Array[Byte]): SingleParameter = single(it)
  implicit def fromArray[A](it: Array[A])(implicit sp: SP[A]): TupleParameter = new TupleParameter(it.map(sp))
  implicit def fromIterable[A](it: Iterable[A])(implicit sp: SP[A]): TupleParameter = new TupleParameter(it.map(sp))
  implicit def fromTuple1[T1](t: Tuple1[T1])(implicit sp1: SP[T1]): TupleParameter = TupleParameter(sp1(t._1))
  implicit def fromTuple2[T1, T2](t: Tuple2[T1, T2])(implicit sp1: SP[T1], sp2: SP[T2]): TupleParameter =
    TupleParameter(sp1(t._1), sp2(t._2))
  implicit def fromTuple3[T1, T2, T3](
    t: Tuple3[T1, T2, T3]
  )(implicit sp1: SP[T1], sp2: SP[T2], sp3: SP[T3]): TupleParameter =
    TupleParameter(sp1(t._1), sp2(t._2), sp3(t._3))
  implicit def fromTuple4[T1, T2, T3, T4](
    t: Tuple4[T1, T2, T3, T4]
  )(implicit sp1: SP[T1], sp2: SP[T2], sp3: SP[T3], sp4: SP[T4]): TupleParameter =
    TupleParameter(sp1(t._1), sp2(t._2), sp3(t._3), sp4(t._4))
  implicit def fromTuple5[T1, T2, T3, T4, T5](
    t: Tuple5[T1, T2, T3, T4, T5]
  )(implicit sp1: SP[T1], sp2: SP[T2], sp3: SP[T3], sp4: SP[T4], sp5: SP[T5]): TupleParameter =
    TupleParameter(sp1(t._1), sp2(t._2), sp3(t._3), sp4(t._4), sp5(t._5))
  implicit def fromTuple6[T1, T2, T3, T4, T5, T6](
    t: Tuple6[T1, T2, T3, T4, T5, T6]
  )(implicit sp1: SP[T1], sp2: SP[T2], sp3: SP[T3], sp4: SP[T4], sp5: SP[T5], sp6: SP[T6]): TupleParameter =
    TupleParameter(sp1(t._1), sp2(t._2), sp3(t._3), sp4(t._4), sp5(t._5), sp6(t._6))
  implicit def fromTuple7[T1, T2, T3, T4, T5, T6, T7](
    t: Tuple7[T1, T2, T3, T4, T5, T6, T7]
  )(implicit
    sp1: SP[T1],
    sp2: SP[T2],
    sp3: SP[T3],
    sp4: SP[T4],
    sp5: SP[T5],
    sp6: SP[T6],
    sp7: SP[T7]
  ): TupleParameter =
    TupleParameter(sp1(t._1), sp2(t._2), sp3(t._3), sp4(t._4), sp5(t._5), sp6(t._6), sp7(t._7))
  implicit def fromTuple8[T1, T2, T3, T4, T5, T6, T7, T8](
    t: Tuple8[T1, T2, T3, T4, T5, T6, T7, T8]
  )(implicit
    sp1: SP[T1],
    sp2: SP[T2],
    sp3: SP[T3],
    sp4: SP[T4],
    sp5: SP[T5],
    sp6: SP[T6],
    sp7: SP[T7],
    sp8: SP[T8]
  ): TupleParameter =
    TupleParameter(sp1(t._1), sp2(t._2), sp3(t._3), sp4(t._4), sp5(t._5), sp6(t._6), sp7(t._7), sp8(t._8))
  implicit def fromTuple9[T1, T2, T3, T4, T5, T6, T7, T8, T9](t: Tuple9[T1, T2, T3, T4, T5, T6, T7, T8, T9])(implicit
    sp1: SP[T1],
    sp2: SP[T2],
    sp3: SP[T3],
    sp4: SP[T4],
    sp5: SP[T5],
    sp6: SP[T6],
    sp7: SP[T7],
    sp8: SP[T8],
    sp9: SP[T9]
  ): TupleParameter =
    TupleParameter(sp1(t._1), sp2(t._2), sp3(t._3), sp4(t._4), sp5(t._5), sp6(t._6), sp7(t._7), sp8(t._8), sp9(t._9))
  implicit def fromTuple10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](
    t: Tuple10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10]
  )(implicit
    sp1: SP[T1],
    sp2: SP[T2],
    sp3: SP[T3],
    sp4: SP[T4],
    sp5: SP[T5],
    sp6: SP[T6],
    sp7: SP[T7],
    sp8: SP[T8],
    sp9: SP[T9],
    sp10: SP[T10]
  ): TupleParameter = TupleParameter(
    sp1(t._1),
    sp2(t._2),
    sp3(t._3),
    sp4(t._4),
    sp5(t._5),
    sp6(t._6),
    sp7(t._7),
    sp8(t._8),
    sp9(t._9),
    sp10(t._10)
  )
  implicit def fromTuple11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11](
    t: Tuple11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11]
  )(implicit
    sp1: SP[T1],
    sp2: SP[T2],
    sp3: SP[T3],
    sp4: SP[T4],
    sp5: SP[T5],
    sp6: SP[T6],
    sp7: SP[T7],
    sp8: SP[T8],
    sp9: SP[T9],
    sp10: SP[T10],
    sp11: SP[T11]
  ): TupleParameter = TupleParameter(
    sp1(t._1),
    sp2(t._2),
    sp3(t._3),
    sp4(t._4),
    sp5(t._5),
    sp6(t._6),
    sp7(t._7),
    sp8(t._8),
    sp9(t._9),
    sp10(t._10),
    sp11(t._11)
  )
  implicit def fromTuple12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12](
    t: Tuple12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12]
  )(implicit
    sp1: SP[T1],
    sp2: SP[T2],
    sp3: SP[T3],
    sp4: SP[T4],
    sp5: SP[T5],
    sp6: SP[T6],
    sp7: SP[T7],
    sp8: SP[T8],
    sp9: SP[T9],
    sp10: SP[T10],
    sp11: SP[T11],
    sp12: SP[T12]
  ): TupleParameter = TupleParameter(
    sp1(t._1),
    sp2(t._2),
    sp3(t._3),
    sp4(t._4),
    sp5(t._5),
    sp6(t._6),
    sp7(t._7),
    sp8(t._8),
    sp9(t._9),
    sp10(t._10),
    sp11(t._11),
    sp12(t._12)
  )
  implicit def fromTuple13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13](
    t: Tuple13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13]
  )(implicit
    sp1: SP[T1],
    sp2: SP[T2],
    sp3: SP[T3],
    sp4: SP[T4],
    sp5: SP[T5],
    sp6: SP[T6],
    sp7: SP[T7],
    sp8: SP[T8],
    sp9: SP[T9],
    sp10: SP[T10],
    sp11: SP[T11],
    sp12: SP[T12],
    sp13: SP[T13]
  ): TupleParameter = TupleParameter(
    sp1(t._1),
    sp2(t._2),
    sp3(t._3),
    sp4(t._4),
    sp5(t._5),
    sp6(t._6),
    sp7(t._7),
    sp8(t._8),
    sp9(t._9),
    sp10(t._10),
    sp11(t._11),
    sp12(t._12),
    sp13(t._13)
  )
  implicit def fromTuple14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14](
    t: Tuple14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14]
  )(implicit
    sp1: SP[T1],
    sp2: SP[T2],
    sp3: SP[T3],
    sp4: SP[T4],
    sp5: SP[T5],
    sp6: SP[T6],
    sp7: SP[T7],
    sp8: SP[T8],
    sp9: SP[T9],
    sp10: SP[T10],
    sp11: SP[T11],
    sp12: SP[T12],
    sp13: SP[T13],
    sp14: SP[T14]
  ): TupleParameter = TupleParameter(
    sp1(t._1),
    sp2(t._2),
    sp3(t._3),
    sp4(t._4),
    sp5(t._5),
    sp6(t._6),
    sp7(t._7),
    sp8(t._8),
    sp9(t._9),
    sp10(t._10),
    sp11(t._11),
    sp12(t._12),
    sp13(t._13),
    sp14(t._14)
  )
  implicit def fromTuple15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15](
    t: Tuple15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15]
  )(implicit
    sp1: SP[T1],
    sp2: SP[T2],
    sp3: SP[T3],
    sp4: SP[T4],
    sp5: SP[T5],
    sp6: SP[T6],
    sp7: SP[T7],
    sp8: SP[T8],
    sp9: SP[T9],
    sp10: SP[T10],
    sp11: SP[T11],
    sp12: SP[T12],
    sp13: SP[T13],
    sp14: SP[T14],
    sp15: SP[T15]
  ): TupleParameter = TupleParameter(
    sp1(t._1),
    sp2(t._2),
    sp3(t._3),
    sp4(t._4),
    sp5(t._5),
    sp6(t._6),
    sp7(t._7),
    sp8(t._8),
    sp9(t._9),
    sp10(t._10),
    sp11(t._11),
    sp12(t._12),
    sp13(t._13),
    sp14(t._14),
    sp15(t._15)
  )
  implicit def fromTuple16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16](
    t: Tuple16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16]
  )(implicit
    sp1: SP[T1],
    sp2: SP[T2],
    sp3: SP[T3],
    sp4: SP[T4],
    sp5: SP[T5],
    sp6: SP[T6],
    sp7: SP[T7],
    sp8: SP[T8],
    sp9: SP[T9],
    sp10: SP[T10],
    sp11: SP[T11],
    sp12: SP[T12],
    sp13: SP[T13],
    sp14: SP[T14],
    sp15: SP[T15],
    sp16: SP[T16]
  ): TupleParameter = TupleParameter(
    sp1(t._1),
    sp2(t._2),
    sp3(t._3),
    sp4(t._4),
    sp5(t._5),
    sp6(t._6),
    sp7(t._7),
    sp8(t._8),
    sp9(t._9),
    sp10(t._10),
    sp11(t._11),
    sp12(t._12),
    sp13(t._13),
    sp14(t._14),
    sp15(t._15),
    sp16(t._16)
  )
  implicit def fromTuple17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17](
    t: Tuple17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17]
  )(implicit
    sp1: SP[T1],
    sp2: SP[T2],
    sp3: SP[T3],
    sp4: SP[T4],
    sp5: SP[T5],
    sp6: SP[T6],
    sp7: SP[T7],
    sp8: SP[T8],
    sp9: SP[T9],
    sp10: SP[T10],
    sp11: SP[T11],
    sp12: SP[T12],
    sp13: SP[T13],
    sp14: SP[T14],
    sp15: SP[T15],
    sp16: SP[T16],
    sp17: SP[T17]
  ): TupleParameter = TupleParameter(
    sp1(t._1),
    sp2(t._2),
    sp3(t._3),
    sp4(t._4),
    sp5(t._5),
    sp6(t._6),
    sp7(t._7),
    sp8(t._8),
    sp9(t._9),
    sp10(t._10),
    sp11(t._11),
    sp12(t._12),
    sp13(t._13),
    sp14(t._14),
    sp15(t._15),
    sp16(t._16),
    sp17(t._17)
  )
  implicit def fromTuple18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18](
    t: Tuple18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18]
  )(implicit
    sp1: SP[T1],
    sp2: SP[T2],
    sp3: SP[T3],
    sp4: SP[T4],
    sp5: SP[T5],
    sp6: SP[T6],
    sp7: SP[T7],
    sp8: SP[T8],
    sp9: SP[T9],
    sp10: SP[T10],
    sp11: SP[T11],
    sp12: SP[T12],
    sp13: SP[T13],
    sp14: SP[T14],
    sp15: SP[T15],
    sp16: SP[T16],
    sp17: SP[T17],
    sp18: SP[T18]
  ): TupleParameter = TupleParameter(
    sp1(t._1),
    sp2(t._2),
    sp3(t._3),
    sp4(t._4),
    sp5(t._5),
    sp6(t._6),
    sp7(t._7),
    sp8(t._8),
    sp9(t._9),
    sp10(t._10),
    sp11(t._11),
    sp12(t._12),
    sp13(t._13),
    sp14(t._14),
    sp15(t._15),
    sp16(t._16),
    sp17(t._17),
    sp18(t._18)
  )
  implicit def fromTuple19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19](
    t: Tuple19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19]
  )(implicit
    sp1: SP[T1],
    sp2: SP[T2],
    sp3: SP[T3],
    sp4: SP[T4],
    sp5: SP[T5],
    sp6: SP[T6],
    sp7: SP[T7],
    sp8: SP[T8],
    sp9: SP[T9],
    sp10: SP[T10],
    sp11: SP[T11],
    sp12: SP[T12],
    sp13: SP[T13],
    sp14: SP[T14],
    sp15: SP[T15],
    sp16: SP[T16],
    sp17: SP[T17],
    sp18: SP[T18],
    sp19: SP[T19]
  ): TupleParameter = TupleParameter(
    sp1(t._1),
    sp2(t._2),
    sp3(t._3),
    sp4(t._4),
    sp5(t._5),
    sp6(t._6),
    sp7(t._7),
    sp8(t._8),
    sp9(t._9),
    sp10(t._10),
    sp11(t._11),
    sp12(t._12),
    sp13(t._13),
    sp14(t._14),
    sp15(t._15),
    sp16(t._16),
    sp17(t._17),
    sp18(t._18),
    sp19(t._19)
  )
  implicit def fromTuple20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20](
    t: Tuple20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20]
  )(implicit
    sp1: SP[T1],
    sp2: SP[T2],
    sp3: SP[T3],
    sp4: SP[T4],
    sp5: SP[T5],
    sp6: SP[T6],
    sp7: SP[T7],
    sp8: SP[T8],
    sp9: SP[T9],
    sp10: SP[T10],
    sp11: SP[T11],
    sp12: SP[T12],
    sp13: SP[T13],
    sp14: SP[T14],
    sp15: SP[T15],
    sp16: SP[T16],
    sp17: SP[T17],
    sp18: SP[T18],
    sp19: SP[T19],
    sp20: SP[T20]
  ): TupleParameter = TupleParameter(
    sp1(t._1),
    sp2(t._2),
    sp3(t._3),
    sp4(t._4),
    sp5(t._5),
    sp6(t._6),
    sp7(t._7),
    sp8(t._8),
    sp9(t._9),
    sp10(t._10),
    sp11(t._11),
    sp12(t._12),
    sp13(t._13),
    sp14(t._14),
    sp15(t._15),
    sp16(t._16),
    sp17(t._17),
    sp18(t._18),
    sp19(t._19),
    sp20(t._20)
  )
  implicit def fromTuple21[
    T1,
    T2,
    T3,
    T4,
    T5,
    T6,
    T7,
    T8,
    T9,
    T10,
    T11,
    T12,
    T13,
    T14,
    T15,
    T16,
    T17,
    T18,
    T19,
    T20,
    T21
  ](t: Tuple21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21])(implicit
    sp1: SP[T1],
    sp2: SP[T2],
    sp3: SP[T3],
    sp4: SP[T4],
    sp5: SP[T5],
    sp6: SP[T6],
    sp7: SP[T7],
    sp8: SP[T8],
    sp9: SP[T9],
    sp10: SP[T10],
    sp11: SP[T11],
    sp12: SP[T12],
    sp13: SP[T13],
    sp14: SP[T14],
    sp15: SP[T15],
    sp16: SP[T16],
    sp17: SP[T17],
    sp18: SP[T18],
    sp19: SP[T19],
    sp20: SP[T20],
    sp21: SP[T21]
  ): TupleParameter = TupleParameter(
    sp1(t._1),
    sp2(t._2),
    sp3(t._3),
    sp4(t._4),
    sp5(t._5),
    sp6(t._6),
    sp7(t._7),
    sp8(t._8),
    sp9(t._9),
    sp10(t._10),
    sp11(t._11),
    sp12(t._12),
    sp13(t._13),
    sp14(t._14),
    sp15(t._15),
    sp16(t._16),
    sp17(t._17),
    sp18(t._18),
    sp19(t._19),
    sp20(t._20),
    sp21(t._21)
  )
  implicit def fromTuple22[
    T1,
    T2,
    T3,
    T4,
    T5,
    T6,
    T7,
    T8,
    T9,
    T10,
    T11,
    T12,
    T13,
    T14,
    T15,
    T16,
    T17,
    T18,
    T19,
    T20,
    T21,
    T22
  ](
    t: Tuple22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22]
  )(implicit
    sp1: SP[T1],
    sp2: SP[T2],
    sp3: SP[T3],
    sp4: SP[T4],
    sp5: SP[T5],
    sp6: SP[T6],
    sp7: SP[T7],
    sp8: SP[T8],
    sp9: SP[T9],
    sp10: SP[T10],
    sp11: SP[T11],
    sp12: SP[T12],
    sp13: SP[T13],
    sp14: SP[T14],
    sp15: SP[T15],
    sp16: SP[T16],
    sp17: SP[T17],
    sp18: SP[T18],
    sp19: SP[T19],
    sp20: SP[T20],
    sp21: SP[T21],
    sp22: SP[T22]
  ): TupleParameter = TupleParameter(
    sp1(t._1),
    sp2(t._2),
    sp3(t._3),
    sp4(t._4),
    sp5(t._5),
    sp6(t._6),
    sp7(t._7),
    sp8(t._8),
    sp9(t._9),
    sp10(t._10),
    sp11(t._11),
    sp12(t._12),
    sp13(t._13),
    sp14(t._14),
    sp15(t._15),
    sp16(t._16),
    sp17(t._17),
    sp18(t._18),
    sp19(t._19),
    sp20(t._20),
    sp21(t._21),
    sp22(t._22)
  )

  implicit def fromTuples[A](seq: Seq[A])(implicit tp: A => TupleParameter): TuplesParameter = new TuplesParameter(
    seq.map(tp)
  )
}

trait SingleParameter extends Parameter {
  protected[this] def set(statement: PreparedStatement, i: Int): Unit

  def placeholder = "?"
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

class TupleParameter(_params: Iterable[SingleParameter]) extends MultipleParameter {
  // get a `Seq` to make sure placeholder and parameterize get the same ordering
  override protected val params = _params.toSeq
  def placeholder = params.iterator.map(_.placeholder).mkString(",")
}

object TupleParameter {
  def apply(params: SingleParameter*) = new TupleParameter(params)
}

class TuplesParameter(_params: Iterable[TupleParameter]) extends MultipleParameter {
  override protected val params = _params.toSeq
  def placeholder = if (params.isEmpty) "" else params.map(_.placeholder).mkString("(", "),(", ")")
}
