package com.lucidchart.open.relate

import org.scalameter.api._
import java.sql._

trait DbBench { self: Bench.HTMLReport =>
  implicit val conn = Init.conn

  def cases = Seq(RelateTests, AnormTests, JdbcTests)

  def test(name: String)(f: TestCase => Any): Unit = {
    performance of name in {
      cases.foreach { c =>
        measure method c.name in {
          f(c)
        }
      }
    }
  }
}

trait DbReport extends Bench.OfflineReport with DbBench

object Benchmarks extends DbReport {
  val ranges = for (size <- Gen.range("rows")(100, 10000, 1980)) yield 0 until size
  val rows = for (size <- Gen.range("rows")(1000, 10000, 1800)) yield 0 until size

  test("Selecting 1 column") { c =>
    using(ranges) in { nums =>
      c.oneColumn(nums)
    }
  }

  test("Selecting 10 columns") { c =>
    using(ranges) in { nums =>
      c.tenColumns(nums)
    }
  }

  test("Selecting 25 columns") { c =>
    using(ranges) in { nums =>
      c.twentyFiveColumns(nums)
    }
  }

  test("Inserting 10 columns") { c =>
    using(rows) in { i =>
      c.insertTen(i)
    }
  }

  test("Inserting 50 columns") { c =>
    using(rows) in { i =>
      c.insertFifty(i)
    }
  }

  test("Updating 2 columns") { c =>
    using(rows) in { i =>
      c.updateTwo(i)
    }
  }

  test("Updating 10 columns") { c =>
    using(rows) in { i =>
      c.updateTen(i)
    }
  }

  test("Updating 20 columns") { c =>
    using(rows) in { i =>
      c.updateTwenty(i)
    }
  }
}

trait TestCase extends Serializable {
  def name: String
  def oneColumn(nums: Seq[Int])(implicit conn: Connection): List[Int]
  def tenColumns(nums: Seq[Int])(implicit conn: Connection): Any
  def twentyFiveColumns(nums: Seq[Int])(implicit conn: Connection): Any
  def insertTen(nums: Seq[Int])(implicit conn: Connection): Any
  def insertFifty(nums: Seq[Int])(implicit conn: Connection): Any
  def updateTwo(nums: Seq[Int])(implicit conn: Connection): Any
  def updateTen(nums: Seq[Int])(implicit conn: Connection): Any
  def updateTwenty(nums: Seq[Int])(implicit conn: Connection): Any
}

object RelateTests extends TestCase {
  import com.lucidchart.open.relate.interp._

  val name: String = "Relate"

  def oneColumn(nums: Seq[Int])(implicit conn: Connection) = {
    sql"SELECT col14 FROM sel_50 WHERE col44 IN ($nums)".asList(_.int("col14"))
  }

  def tenColumns(nums: Seq[Int])(implicit conn: Connection) = {
    sql"SELECT `col45`,`col46`,`col47`,`col48`,`col49`,`col50`,`col1`,`col2`,`col3`,`col4`,`col5` FROM `sel_50` WHERE  `col18` IN ($nums)".asList { row =>
      row.int("col45")
      row.int("col46")
      row.int("col47")
      row.int("col48")
      row.int("col49")
      row.int("col50")
      row.int("col1")
      row.int("col2")
      row.int("col3")
      row.int("col4")
      row.int("col5")
    }
  }

  def twentyFiveColumns(nums: Seq[Int])(implicit conn: Connection) = {
    sql"SELECT `col1`,`col2`,`col3`,`col4`,`col5`,`col6`,`col7`,`col8`,`col9`,`col10`,`col11`,`col12`,`col13`,`col14`,`col15`,`col16`,`col17`,`col18`,`col19`,`col20`,`col21`,`col22`,`col23`,`col24`,`col25` FROM `sel_50` WHERE  `col18` IN ($nums)".asList { row =>
      row.int("col1")
      row.int("col2")
      row.int("col3")
      row.int("col4")
      row.int("col5")
      row.int("col6")
      row.int("col7")
      row.int("col8")
      row.int("col9")
      row.int("col10")
      row.int("col11")
      row.int("col12")
      row.int("col13")
      row.int("col14")
      row.int("col15")
      row.int("col16")
      row.int("col17")
      row.int("col18")
      row.int("col19")
      row.int("col20")
      row.int("col21")
      row.int("col22")
      row.int("col23")
      row.int("col24")
      row.int("col25")
    }
  }

  def insertTen(rows: Seq[Int])(implicit conn: Connection) = {
    rows.foreach { row =>
      sql"INSERT INTO `ins_10` VALUES ($row,$row,$row,$row,$row,$row,$row,$row,$row,$row)".execute()
    }
  }

  def insertFifty(rows: Seq[Int])(implicit conn: Connection) = {
    rows.foreach { row =>
      sql"""INSERT INTO `ins_50` VALUES (
        $row,$row,$row,$row,$row,$row,$row,$row,$row,$row,
        $row,$row,$row,$row,$row,$row,$row,$row,$row,$row,
        $row,$row,$row,$row,$row,$row,$row,$row,$row,$row,
        $row,$row,$row,$row,$row,$row,$row,$row,$row,$row,
        $row,$row,$row,$row,$row,$row,$row,$row,$row,$row
      )""".execute()
    }
  }

  def updateTwo(rows: Seq[Int])(implicit conn: Connection) = {
    sql"UPDATE sel_50 SET col13 = 16, col33 = 19 WHERE col22 IN ($rows)".executeUpdate()
  }

  def updateTen(rows: Seq[Int])(implicit conn: Connection) = {
    sql"UPDATE `sel_50` SET `col1`=16 ,`col43`=19, `col45`=14, `col32`=45, `col26`=254,`col3`=34, `col5`=235, `col12`=5, `col29`=234, `col17`=34 WHERE `col12` IN ($rows)".executeUpdate()
  }

  def updateTwenty(rows: Seq[Int])(implicit conn: Connection) = {
    sql"UPDATE `sel_50` SET `col1`=16 ,`col43`=19, `col45`=14, `col32`=45, `col26`=254,`col3`=34, `col5`=235, `col12`=5, `col29`=234, `col17`=34,`col18`=423, `col19`=341, `col27`=3243, `col28`=2315, `col25`=1234, `col33` = 34126, `col34` = 23425 ,`col35`=342 ,`col11`=234, `col49`=3576 WHERE `col12` IN ($rows)".executeUpdate()
  }
}

object AnormTests extends TestCase {
  import anorm._
  import anorm.SqlParser._

  val name: String = "Anorm"

  def oneColumn(nums: Seq[Int])(implicit conn: Connection) = {
    SQL"SELECT `col14` FROM  `sel_50` WHERE `col44` IN ($nums)".as(int("col14").*)
  }

  def tenColumns(nums: Seq[Int])(implicit conn: Connection) = {
    SQL"SELECT `col45`,`col46`,`col47`,`col48`,`col49`,`col50`,`col1`,`col2`,`col3`,`col4`,`col5` FROM `sel_50` WHERE `col18` IN ($nums)".as(
      (int("col45")~int("col46")~int("col47")~int("col48")~int("col49")~int("col50")~int("col1")~int("col2")~int("col3")~int("col4")~int("col5")).*
    )
  }

  def twentyFiveColumns(nums: Seq[Int])(implicit conn: Connection) = {
    SQL"SELECT `col1`,`col2`,`col3`,`col4`,`col5`,`col6`,`col7`,`col8`,`col9`,`col10`,`col11`,`col12`,`col13`,`col14`,`col15`,`col16`,`col17`,`col18`,`col19`,`col20`,`col21`,`col22`,`col23`,`col24`,`col25` FROM `sel_50`  WHERE `col18` IN ($nums)".as(
      (int("col1")~int("col2")~int("col3")~int("col4")~int("col5")~int("col6")~int("col7")~int("col8")~int("col9")~int("col10")~int("col11")~int("col12")~int("col13")~int("col14")~int("col15")~int("col16")~int("col17")~int("col18")~int("col19")~int("col20")~int("col21")~int("col22")~int("col23")~int("col24")~int("col25")).*
    )
  }

  def insertTen(rows: Seq[Int])(implicit conn: Connection) = {
    rows.foreach { row =>
      SQL"INSERT INTO `ins_10` VALUES ($row,$row,$row,$row,$row,$row,$row,$row,$row,$row)".execute()
    }
  }

  def insertFifty(rows: Seq[Int])(implicit conn: Connection) = {
    rows.foreach { row =>
      SQL"""INSERT INTO `ins_50` VALUES (
        $row,$row,$row,$row,$row,$row,$row,$row,$row,$row,
        $row,$row,$row,$row,$row,$row,$row,$row,$row,$row,
        $row,$row,$row,$row,$row,$row,$row,$row,$row,$row,
        $row,$row,$row,$row,$row,$row,$row,$row,$row,$row,
        $row,$row,$row,$row,$row,$row,$row,$row,$row,$row
      )""".execute()
    }
  }

  def updateTwo(rows: Seq[Int])(implicit conn: Connection) = {
    SQL"UPDATE sel_50 SET col13 = 16, col33 = 19 WHERE col22 IN ($rows)".executeUpdate()
  }

  def updateTen(rows: Seq[Int])(implicit conn: Connection) = {
    SQL"UPDATE `sel_50` SET `col1`=16 ,`col43`=19, `col45`=14, `col32`=45, `col26`=254,`col3`=34, `col5`=235, `col12`=5, `col29`=234, `col17`=34 WHERE `col12` IN ($rows)".executeUpdate()
  }

  def updateTwenty(rows: Seq[Int])(implicit conn: Connection) = {
    SQL"UPDATE `sel_50` SET `col1`=16 ,`col43`=19, `col45`=14, `col32`=45, `col26`=254,`col3`=34, `col5`=235, `col12`=5, `col29`=234, `col17`=34,`col18`=423, `col19`=341, `col27`=3243, `col28`=2315, `col25`=1234, `col33` = 34126, `col34` = 23425 ,`col35`=342 ,`col11`=234, `col49`=3576 WHERE `col12` IN ($rows)".executeUpdate()
  }
}

object JdbcTests extends TestCase {

  val name: String = "JDBC"

  private def withPreparedStatement[A](conn: Connection, query: String)(f:(PreparedStatement) => A): A = {
    var statement: PreparedStatement = null
    try {
      statement = conn.prepareStatement(query)
      f(statement)
    }
    finally {
      if (statement != null) {
        statement.close()
      }
    }
  }

  def oneColumn(nums: Seq[Int])(implicit conn: Connection) = {
    val queryBuilder = new StringBuilder((nums.size*2)+100,"SELECT `col14` FROM  `sel_50` WHERE `col44` IN (?" )
    var i = 1
    while (i < nums.size) {
      queryBuilder.append(",?")
      i += 1
    }
    queryBuilder.append(")")

    withPreparedStatement(conn,  queryBuilder.toString) { statement =>
      var i = 1
      while (i <= nums.size) {
        statement.setInt(i, nums(i-1))
        i += 1
      }

      statement.executeQuery()
      List()
    }
  }

  def tenColumns(nums: Seq[Int])(implicit conn: Connection) = {
    val queryBuilder = new StringBuilder((nums.size*2)+100,"SELECT `col45`,`col46`,`col47`,`col48`,`col49`,`col50`,`col1`,`col2`,`col3`,`col4`,`col5` FROM `sel_50` WHERE  `col18` IN (?" )
    var i = 1
    while (i < nums.size) {
      queryBuilder.append(",?")
      i += 1
    }
    queryBuilder.append(")")

    withPreparedStatement(conn,  queryBuilder.toString) { statement =>
      var i = 1
      while (i <= nums.size) {
        statement.setInt(i, nums(i-1))
        i += 1
      }
      statement.executeQuery()
    }
  }

  def twentyFiveColumns(nums: Seq[Int])(implicit conn: Connection) = {
    val queryBuilder = new StringBuilder((nums.size*2)+100,"SELECT `col1`,`col2`,`col3`,`col4`,`col5`,`col6`,`col7`,`col8`,`col9`,`col10`,`col11`,`col12`,`col13`,`col14`,`col15`,`col16`,`col17`,`col18`,`col19`,`col20`,`col21`,`col22`,`col23`,`col24`,`col25` FROM `sel_50` WHERE  `col18` IN (?" )
    var i = 1
    while (i < nums.size) {
      queryBuilder.append(",?")
      i += 1
    }
    queryBuilder.append(")")

    withPreparedStatement(conn,  queryBuilder.toString) { statement =>

      var i = 1
      while (i <= nums.size) {
        statement.setInt(i, nums(i-1))
        i += 1
      }
      statement.executeQuery()
    }
  }

  def insertTen(rows: Seq[Int])(implicit conn: Connection) = {
    val cols = 10
    val queryBuilder = new StringBuilder((cols*2)+100,"INSERT INTO `ins_10` VALUES  (?" )
    var i = 1
    while (i < cols) {
      queryBuilder.append(",?")
      i += 1
    }
    queryBuilder.append(")")
    rows.foreach { row =>
      withPreparedStatement(conn,  queryBuilder.toString) { statement =>
        var i = 1
        while (i <= cols) {
          statement.setInt(i, row)
          i += 1
        }
        statement.executeUpdate()
      }
    }
  }

  def insertFifty(rows: Seq[Int])(implicit conn: Connection) = {
    val cols = 50
    val queryBuilder = new StringBuilder((cols*2)+100,"INSERT INTO `ins_50` VALUES  (?" )
    var i = 1
    while (i < cols) {
      queryBuilder.append(",?")
      i += 1
    }
    queryBuilder.append(")")
    rows.foreach { row =>
      withPreparedStatement(conn,  queryBuilder.toString)
      { statement =>
        var i = 1
        while (i <= cols) {
          statement.setInt(i, row)
          i += 1
        }
        statement.executeUpdate()
      }
    }
  }

  def updateTwo(nums: Seq[Int])(implicit conn: Connection) = {
    val queryBuilder = new StringBuilder((nums.size*2)+100,"UPDATE `sel_50` SET `col13`=16 ,`col33`=19 where `col22` IN (?" )
    var i = 1
    while (i < nums.size) {
      queryBuilder.append(",?")
      i += 1
    }
    queryBuilder.append(")")

    withPreparedStatement(conn,  queryBuilder.toString) { statement =>
      var i = 1
      while (i <= nums.size) {
        statement.setInt(i, nums(i-1))
        i += 1
      }
      statement.executeUpdate()
    }
  }

  def updateTen(nums: Seq[Int])(implicit conn: Connection) = {
    val queryBuilder = new StringBuilder((nums.size*2)+100,"UPDATE `sel_50` SET `col1`=16 ,`col43`=19, `col45`=14, `col32`=45, `col26`=254,`col3`=34, `col5`=235, `col12`=5, `col29`=234, `col17`=34 where `col12` IN  (?" )
    var i = 1
    while (i < nums.size) {
      queryBuilder.append(",?")
      i += 1
    }
    queryBuilder.append(")")

    withPreparedStatement(conn,  queryBuilder.toString) { statement =>
      var i = 1
      while (i <= nums.size) {
        statement.setInt(i, nums(i-1))
        i += 1
      }

      statement.executeUpdate()
    }
  }

  def updateTwenty(nums: Seq[Int])(implicit conn: Connection) = {
    val queryBuilder = new StringBuilder((nums.size*2)+100,"UPDATE `sel_50` SET `col1`=16 ,`col43`=19, `col45`=14, `col32`=45, `col26`=254,`col3`=34, `col5`=235, `col12`=5, `col29`=234, `col17`=34,`col18`=423, `col19`=341, `col27`=3243, `col28`=2315, `col25`=1234, `col33` = 34126, `col34` = 23425, `col35`=342 ,`col11`=234, `col49`=3576 where `col12` IN  (?" )
    var i = 1
    while (i < nums.size) {
      queryBuilder.append(",?")
      i += 1
    }
    queryBuilder.append(")")

    withPreparedStatement(conn,  queryBuilder.toString) { statement =>
      var i = 1
      while (i <= nums.size) {
        statement.setInt(i, nums(i-1))
        i += 1
      }

      statement.executeUpdate()
    }
  }
}