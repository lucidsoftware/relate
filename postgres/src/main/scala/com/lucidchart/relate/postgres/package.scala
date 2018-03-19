package com.lucidchart.relate

import java.sql.Types
import org.postgresql.util.PGInterval
import scala.concurrent.duration.FiniteDuration

package object postgres {

  implicit val pgIntervalParameterizable =
    Parameterizable(_.setObject(_, _: PGInterval), _.setNull(_, Types.JAVA_OBJECT))

  implicit val finiteDurationParameterizable =
    Parameterizable.from((value: FiniteDuration) => new PGInterval(0, 0, 0, 0, 0, value.toSeconds))

}
