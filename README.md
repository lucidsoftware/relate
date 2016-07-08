# Relate

[![Join the chat at https://gitter.im/lucidsoftware/relate](https://badges.gitter.im/lucidsoftware/relate.svg)](https://gitter.im/lucidsoftware/relate?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![Build Status](https://travis-ci.org/lucidsoftware/relate.svg)](https://travis-ci.org/lucidsoftware/relate)

Relate is a lightweight, blazingly fast database access layer for Scala that abstracts the idiosyncricies of the JDBC while keeping complete control over the SQL.

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


