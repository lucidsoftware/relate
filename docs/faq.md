---
layout: doc
title: FAQ
subtitle: Answers to all your questions, except maybe the Ultimate Question of Life, the Universe, and Everything
previous:
  name: Retrieving Data
  url: /docs/retrieving-data.html
next:
  name: Browse API Documentation
  url: /api/1.13.0/index.html
---
Here are a list of questions that people have asked about Relate in the past. If you have a question you'd like answered (and maybe put here), please [create a Github issue in the repo](https://github.com/lucidsoftware/relate/issues/new).

### Q: How do I interpolate table names into a query? `$tableName` makes it a parameter <span class="mega-octicon octicon-flame"></span>

A: This can be done by using [Query Composition]({{site.baseurl}}):

{% highlight scala %}
val tableName = if (good) sql"jedi" else sql"sith"
sql"INSERT INTO $tableName VALUES ..."
{% endhighlight %}

### Q: I don't trust you one bit. How do I run the benchmarks?

A: The [Relate repo](https://github.com/lucidsoftware/relate) contains the benchmarks we use to make sure we don't regress when adding features. Just clone the repo, fire up SBT, and run `bench:test`. You'll find the reports in `target/benchmarks/reports/index.html`. The benchmarks were built with [Scalameter](http://scalameter.github.io/).

### Q: Why can't I interpolate &lt;insert type here&gt;?

A: Relate only supports interpolation for the types supported by JDBC.
