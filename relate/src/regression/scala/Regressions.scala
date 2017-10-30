package com.lucidchart.relate

import org.scalameter.api._

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

    measure method "one column (alternate method)" in {
      using(ranges) in { nums =>
        selectIterations.foreach { _ =>
          RelateTests.oneColumnAlt(nums)
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

    measure method "ten columns (alternate method)" in {
      using(ranges) in { nums =>
        selectIterations.foreach { _ =>
          RelateTests.tenColumnsAlt(nums)
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

    measure method "twenty-five columns (alternate method)" in {
      using(ranges) in { nums =>
        selectIterations.foreach { _ =>
          RelateTests.tenColumnsAlt(nums)
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
    measure method "unsafeSql" in {
      using(unit) in { _ =>
        interpIterations.foreach { _ =>
          val s = "SELECT".unsafeSql
          val f = "FROM".unsafeSql
          val w = "WHERE".unsafeSql
          sql"$s * $f table $w 1 = 1"
        }
      }
    }
  }

  type ThreeCol = (Int, Int, Int)
  implicit val threeColRowParser = new RowParser[ThreeCol] {
    def parse(row: SqlRow): ThreeCol = (
      row.int("col1"),
      row.int("col2"),
      row.int("col3")
    )
  }

  type TwentyTwoCol = (Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)
  implicit val twentyTwoColRowParser = new RowParser[TwentyTwoCol] {
    def parse(row: SqlRow): TwentyTwoCol = (
      row.int("col1"),
      row.int("col2"),
      row.int("col3"),
      row.int("col4"),
      row.int("col5"),
      row.int("col6"),
      row.int("col7"),
      row.int("col8"),
      row.int("col9"),
      row.int("col10"),
      row.int("col11"),
      row.int("col12"),
      row.int("col13"),
      row.int("col14"),
      row.int("col15"),
      row.int("col16"),
      row.int("col17"),
      row.int("col18"),
      row.int("col19"),
      row.int("col20"),
      row.int("col21"),
      row.int("col22")
    )
  }

  val query = sql"SELECT * FROM sel_50"
  val parserIterations = (0 until 10000)

  performance of "parsers" in {
    performance of "three columns" in {
      measure method "asList(parser)" in {
        using(Gen.unit("parser")) in { _ =>
          parserIterations.foreach { _ =>
            query.asList(threeColRowParser.parse)
          }
        }
      }

      measure method "asList[Type]" in {
        using(Gen.unit("parser")) in { _ =>
          parserIterations.foreach { _ =>
            query.asList[ThreeCol]
          }
        }
      }

      measure method "as[List[Type]]" in {
        using(Gen.unit("parser")) in { _ =>
          parserIterations.foreach { _ =>
            query.as[List[ThreeCol]]
          }
        }
      }
    }

    performance of "thirty columns" in {
      measure method "asList(parser)" in {
        using(Gen.unit("parser")) in { _ =>
          parserIterations.foreach { _ =>
            query.asList(twentyTwoColRowParser.parse)
          }
        }
      }

      measure method "asList[Type]" in {
        using(Gen.unit("parser")) in { _ =>
          parserIterations.foreach { _ =>
            query.asList[TwentyTwoCol]
          }
        }
      }

      measure method "as[List[Type]]" in {
        using(Gen.unit("parser")) in { _ =>
          parserIterations.foreach { _ =>
            query.as[List[TwentyTwoCol]]
          }
        }
      }
    }
  }
}
