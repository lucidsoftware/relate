---
layout: doc
title: Retrieving Data
subtitle: In case you actually wanted to get data, instead of just feeling the thrill of writing nice looking queries
previous:
  name: Writing Queries
  url: /docs/writing-queries.html
next:
  name: FAQ
  url: /docs/faq.html
---
Executing a query when you don't care about the result is as simple as:

{% highlight scala %}
// Death Star Query
sql"""DELETE FROM planets WHERE name="Alderaan"""".execute
{% endhighlight %}

However, when you execute a query, you usually want to interact with the result in some way in Scala. To do this in Relate, do this by defining a parser and passing it to one of many methods that will parse rows into the specified data structure (`List`, `Map`, etc.). In the following sections, we'll show how to define parsers and how to use them.

## Defining a Parser

Parsers are nothing more than functions that take a [`SqlResult`]({{site.baseurl}}/api/1.13.0/index.html#com.lucidchart.open.relate.SqlResult) representing a row of data and return the desired result. `SqlResult`s have many methods that allow you to extract a column as an instance particular type. Here's an example:

{% highlight scala %}
import com.lucidchart.open.relate.SqlResult

object Color extends Enumeration {
  val Green = Value(0)
  val Blue = Value(1)
  val Red = Value(2)
}

case class Jedi(
  name: String,
  lightSaberColor: Color.Value,
  species: Option[String]
)

val jediParser = { row: SqlResult =>
  Jedi(
    row.string("name"),
    Color(row.short("lightsaber_color")),
    row.stringOption("species")
  )
}
{% endhighlight %}

In this example, the created parser takes the value from the `name` column of the row as a string, the value of `lightSaberColor` as a short that is used for the `Enumeration`, and the value from the `species` column as a string option to instantiate a Jedi object. The `SqlResult` object has [numerous methods to extract data]({{site.baseurl}}/api/1.13.0/index.html#com.lucidchart.open.relate.SqlResult) from the row with the desired data type.

The parser can return any value, so it doesn't necessarily need to be an instance of a case class. A `Tuple`, `Seq`, etc. would work equally well.

## Applying a Parser

Applying a parser to a query only requires specifying the desired collection type to return. The following is an example using the parser created in the previous section:

{% highlight scala %}
sql"SELECT * FROM jedi".asList(jediParser)(connection)
{% endhighlight %}

This example would return a `List[Jedi]`. The parser can also be passed to the `asSingle`, `asSingleOption`, `asSet`, `asSeq`, `asIterable`, and `asList` methods to produce the respective collections.

Parsers that return a Tuple of size 2 can also be passed to the `asMap` method to get a Map. Here's an example of its use (using the case class from the previous example):

{% highlight scala %}
val nameToJediParser = { row: SqlResult =>
  val jedi = Jedi(
    row.string("name"),
    Color(row.short("lightsaber_color")),
    row.stringOption("species")
  )
  (jedi.name, jedi)
}

// Map[String, Jedi]
sql"SELECT * FROM jedi".asMap(nameToJediParser)(connection)
{% endhighlight %}

## Single Column Parsers

Sometimes a query retrieves only one column. Convenience methods are defined in [`RowParser`]({{site.baseurl}}/api/1.13.0/index.html#com.lucidchart.open.relate.RowParser$) for creating single column row parsers in these occasions. Below is an example of their use:

{% highlight scala %}
import com.lucidchart.open.relate.RowParser

sql"""
  SELECT name
  FROM jedi
  WHERE species="Human"
""".asList(RowParser.string("id"))
{% endhighlight %}

The [`RowParser`]({{site.baseurl}}/api/1.13.0/index.html#com.lucidchart.open.relate.RowParser$) object also contains definitions for `bigInt`, `date`, `int`, and `string`.

## Single Value Parsers

In other cases, only one value is desired as the result of a query. For these scenarios, Relate provides a [scalar method]({{site.baseurl}}/api/1.13.0/index.html#com.lucidchart.open.relate.Sql@asScalarOption[A]()(implicitconnection:java.sql.Connection):Option[A]) with which the desired type of the returned single value can be defined. The return value is wrapped as an `Option`. An example of its use is as follows:

{% highlight scala %}
sql"""
  SELECT species
  FROM jedi
  WHERE name="Yoda"
""".asScalarOption[String]
{% endhighlight %}

There is also a non-option version of this method, [`asScalar[A]`]({{site.baseurl}}/api/1.13.0/index.html#com.lucidchart.open.relate.Sql@asScalar[A]()(implicitconnection:java.sql.Connection):A).

## Retrieving Auto Increment Values on Insert

There also exist [methods to retrieve the auto-incremented ids]({{site.baseurl}}/api/1.13.0/index.html#com.lucidchart.open.relate.Sql) of inserted records. Given a table where the primary key was a bigint, here's an example:

{% highlight scala %}
val id = sql"""
  INSERT INTO droids(name)
  VALUES ("BB-8")
""".executeInsertLong()
{% endhighlight %}

Ids can also be retrieved as integers with `executeInsertInt`. There also exist plural versions of these two methods: `executeInsertInts` and `executeInsertLongs`. Finally, in the case that the id is not an integer or bigint, use `executeInsertSingle` or `executeInsertCollection`, both of which take parsers capable of parsing your ids as their parameters.
