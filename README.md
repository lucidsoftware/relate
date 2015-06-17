# Relate

Relate is a lightweight, blazing-fast database access layer for Scala that abstracts the idiosyncricies of the JDBC while keeping complete control over the SQL.

## Examples

```scala
val ids = Seq(1, 2, 3)
sql"SELECT email FROM users WHERE id in ($ids)".asMap { row =>
  row.long("id") -> row.string("email")
}
```

```scala
val id = 4
val email = "github@lucidchart.com"
sql"INSERT INTO users VALUES ($id, $email)".execute()
```

## Motivation

Relate is for developers who care about their database. It chooses not to sacrifice two things: SQL and performance.

* SQL is already the best domain-specific language for querying relational databases. We don't need another. Ultimately, ORMs obscure the underlying queries and cause lots of headache.

* In production situations, every millisecond matters. And while Relate may not be the prettiest face in the crowd, it does the best at managing large queries and large results.

With Relate, you can write your queries in the language nature intended, while interacting with the inputs and results in a high-level and performant way.

## SBT

To use Relate with sbt, add this to your build.sbt or Build.scala:

```scala
libraryDependencies += "com.lucidchart" %% "relate" % "1.7.1"

resolvers += "Sonatype release repository" at "https://oss.sonatype.org/content/repositories/releases/"
```

[Continue to Documentation](https://github.com/lucidsoftware/relate/wiki)

[![Build Status](https://travis-ci.org/lucidsoftware/relate.svg)](https://travis-ci.org/lucidsoftware/relate)
