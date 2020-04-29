# Relate
http://lucidsoftware.github.io/relate/

[![Build Status](https://travis-ci.com/lucidsoftware/relate.svg)](https://travis-ci.com/lucidsoftware/relate)
[![Maven Version](https://img.shields.io/maven-central/v/com.lucidchart/relate_2.12.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.lucidchart%22%20AND%20a%3A%22relate_2.12%22)
[![Join the chat at https://gitter.im/lucidsoftware/relate](https://badges.gitter.im/lucidsoftware/relate.svg)](https://gitter.im/lucidsoftware/relate?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Relate is a lightweight, blazingly fast database access layer for Scala that abstracts the idiosyncricies of the JDBC while keeping complete control over the SQL.

## Install

```scala
libraryDependencies += "com.lucidchart" %% "relate" % "<version>"
```

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

[Continue to Documentation](http://lucidsoftware.github.io/relate/)


