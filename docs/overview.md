---
layout: doc
title: Overview
subtitle: What's Relate?
previous:
  name: Home
  url: /
next:
  name: Getting Started
  url: /docs/getting-started.html
---
Relate is a lightweight database access layer for Scala that simplifies database interaction while leaving complete control over the actual SQL query. It was developed at [Lucid Software](https://golucid.co) in response to a need for increasingly performant database access software and seeks to abstract away `PreparedStatement`'s idiosyncrasies while maintaining its speed.

## Advantages - Why Use Relate?
* Works with all JDBC connection types
* Compatible with all database engines
* Performance

## Constraints - What Relate Doesn't Do
* Create connections to databases
* ORM abstraction

## Next Steps
* [Getting Started]({{site.baseurl}}/docs/getting-started.html)
* [Writing Queries]({{site.baseurl}}/docs/writing-queries.html)
  * [Queries without Parameters]({{site.baseurl}}/docs/writing-queries.html#basic-queries)
  * [Queries with Parameter Interpolation]({{site.baseurl}}/docs/writing-queries.html#basic-parameter-interpolation)
  * [Parameter Sequences]({{site.baseurl}}/docs/writing-queries.html#parameter-sequences)
  * [Query Composition]({{site.baseurl}}/docs/writing-queries.html#query-composition)
* [Retrieving Data]({{site.baseurl}}/docs/retrieving-data.html)
* [FAQ]({{site.baseurl}}/docs/faq.html)
* [Browse API Docs]({{site.baseurl}}/docs/api/index.html)
