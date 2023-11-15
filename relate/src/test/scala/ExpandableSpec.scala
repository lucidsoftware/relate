package com.lucidchart.relate.test

import org.specs2.mutable._

class ExpandableSpec extends Specification {

  // "The expand method" should {

  // "work with one param" in {
  //   val query = ExpandableQuery("SELECT * FROM table WHERE id IN ({ids})").expand { implicit query =>
  //     commaSeparated("ids", 3)
  //   }.query

  //   query must_== "SELECT * FROM table WHERE id IN ({ids_0},{ids_1},{ids_2})"
  // }

  // "work with multiple params" in {
  //   val query = ExpandableQuery("SELECT * FROM table WHERE id IN ({ids}) AND value IN ({values})").expand { implicit query =>
  //     commaSeparated("values", 2)
  //     commaSeparated("ids", 3)
  //   }.query

  //   query must_== "SELECT * FROM table WHERE id IN ({ids_0},{ids_1},{ids_2}) AND value IN ({values_0},{values_1})"
  // }

  // "work with a mix of expandable and normal 'on' parameters" in {
  //   val query = ExpandableQuery("SELECT * FROM table WHERE id IN ({ids}) AND name={name}").expand { implicit query =>
  //     commaSeparated("ids", 3)
  //   }.query

  //   query must_== "SELECT * FROM table WHERE id IN ({ids_0},{ids_1},{ids_2}) AND name={name}"
  // }

  // "work with chained 'expand' method calls" in {
  //   val query = ExpandableQuery("SELECT * FROM table WHERE id IN ({ids}) AND value IN ({values})").expand { implicit query =>
  //     commaSeparated("values", 2)
  //   }.expand { implicit query =>
  //     commaSeparated("ids", 3)
  //   }.query

  //   query must_== "SELECT * FROM table WHERE id IN ({ids_0},{ids_1},{ids_2}) AND value IN ({values_0},{values_1})"
  // }

  // }

}
