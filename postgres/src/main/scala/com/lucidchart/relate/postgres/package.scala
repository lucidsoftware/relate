package com.lucidchart.relate

import org.postgresql.util.PGInterval
import scala.concurrent.duration.FiniteDuration
import java.sql.PreparedStatement

package object postgres {

  implicit class PGIntervalParameter(value: PGInterval) extends SingleParameter {
    protected def set(stmt: PreparedStatement, i: Int) = stmt.setObject(i, value)
  }

  implicit class FiniteDurationParameter(value: FiniteDuration)
      extends PGIntervalParameter(new PGInterval(0, 0, 0, 0, 0, value.toSeconds))

}
