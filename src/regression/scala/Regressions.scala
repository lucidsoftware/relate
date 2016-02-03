package com.lucidchart.open.relate

import org.scalameter.api._
import java.sql._
import com.lucidchart.open.relate.interp._

trait DbRegression extends Bench.OfflineRegressionReport with DbBench {
  def test(c: TestCase, name: String)(f: TestCase => Any): Unit = {
    performance of name in {
      measure method c.name in {
        f(c)
      }
    }
  }
}

object Regressions extends DbRegression {
  val ranges = for (size <- Gen.range("rows")(10, 10000, 370)) yield 0 until size
  val rows = for (size <- Gen.range("rows")(1000, 10000, 1800)) yield 0 until size

  val selectIterations = (0 until 10)

  performance of "Select" in {
    measure method "one column" in {
      using(ranges) in { nums =>
        selectIterations.foreach { _ =>
          RelateTests.oneColumn(nums)
        }
      }
    }

    measure method "ten columns" in {
      using(ranges) in { nums =>
        selectIterations.foreach { _ =>
          RelateTests.tenColumns(nums)
        }
      }
    }

    measure method "twenty-five columns" in {
      using(ranges) in { nums =>
        selectIterations.foreach { _ =>
          RelateTests.tenColumns(nums)
        }
      }
    }
  }

  performance of "Insert" in {
    measure method "ten columns" in {
      using(ranges) in { nums =>
        RelateTests.insertTen(nums)
      }
    }

    measure method "fifty columns" in {
      using(ranges) in { nums =>
        RelateTests.insertFifty(nums)
      }
    }
  }

  performance of "Update" in {
    measure method "two columns" in {
      using(rows) in { nums =>
        RelateTests.updateTwo(nums)
      }
    }

    measure method "ten columns" in {
      using(rows) in { nums =>
        RelateTests.updateTen(nums)
      }
    }

    measure method "twenty columns" in {
      using(rows) in { nums =>
        RelateTests.updateTwenty(nums)
      }
    }
  }

  val interpIterations = (0 until 10000)
  val unit = Gen.unit("interp")

  performance of "Interpolation" in {
    measure method "toSql" in {
      using(unit) in { _ =>
        interpIterations.foreach { _ =>
          val s = "SELECT".toSql
          val f = "FROM".toSql
          val w = "WHERE".toSql
          sql"$s * $f table $w 1 = 1"
        }
      }
    }
  }
}
