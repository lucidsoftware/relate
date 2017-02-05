---
layout: doc
title: Writing Queries
subtitle: Doctors hate them for using this one weird database access library to pimp out their SQL
previous:
  name: Getting Started
  url: /docs/getting-started.html
next:
  name: Retrieving Data
  url: /docs/retrieving-data.html
---
## Basic Queries

Queries are created using the custom [string interpolation method](http://docs.scala-lang.org/overviews/core/string-interpolation.html#advanced-usage) `sql`:

{% highlight scala %}
import com.lucidchart.relate._

sql"SELECT name, lightsaber_color FROM jedi"
{% endhighlight %}

## Basic Parameter Interpolation

Parameterized queries are created by using interpolated parameters:

{% highlight scala %}
val id = 5
sql"SELECT * FROM imperial_army WHERE id = $id"
{% endhighlight %}

Relate uses JDBC's `PreparedStatement` to correctly escape inputs.

{% highlight scala %}
val name = "X-Wing'; DROP TABLE jedi;"
sql"SELECT * FROM ships WHERE name = $name" // nice try, Siths!
{% endhighlight %}

## Parameter sequences

Sequences of values are inserted as comma-separated lists.

{% highlight scala %}
val weWishWeCouldForget = Seq("Jar Jar", "Boss Nass")
sql"DELETE FROM characters WHERE name IN ($weWishWeCouldForget)"
// becomes DELETE FROM characters WHERE name IN ("Jar Jar", "Boss Nass")
{% endhighlight %}

Sequences of sequences of values are inserted as comma-separated tuples.

{% highlight scala %}
val stormtroopers = Seq(
  Seq("TK-132131", "Stormtrooper"),
  Seq("FN-2187", "Snowtrooper")
)
sql"INSERT INTO imperial_army (id, armor) VALUES $stormtroopers"

// becomes INSERT INTO imperial_army (id, armor)
// VALUES ("TK-132131","Stormtrooper"), ("FN-2187","Snowtrooper")
{% endhighlight %}

Types must be visible at compile time and correspond to parameterizable types.
The following does not compile, as Relate cannot parameterize `Seq[Seq[Any]]`.

{% highlight scala %}
val users = Seq(Seq(4, "yoda@jedicouncil.com"), Seq(5, "palpatine@secretsith.org"))
sql"INSERT INTO users (id, email) VALUES $users" // DOES NOT COMPILE
{% endhighlight %}

Tuples should be used instead.

{% highlight scala %}
val users = Seq((4, "yoda@jedicouncil.com"), (5, "palpatine@secretsith.org"))
sql"INSERT INTO users (id, email) VALUES $users"
{% endhighlight %}

## Query composition

For more dynamic queries, SQL statements can be composed with interpolation

{% highlight scala %}
val sql1 = sql"SELECT * FROM jedi"
val sql2 = sql"$sql1 LIMIT 5"
{% endhighlight %}

or concatenation

{% highlight scala %}
val sql1 = sql"SELECT * FROM jedi"
val sql2 = sql" LIMIT 5"
val sql3 = sql1 + sql2
{% endhighlight %}
