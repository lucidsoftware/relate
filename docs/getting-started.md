---
layout: doc
title: Getting Started
subtitle: Including the Relate in your project and general information about getting started with Relate
previous:
  name: Overview
  url: /docs/overview.html
next:
  name: Writing Queries
  url: /docs/writing-queries.html
---
## Step 1: SBT
Getting started with Relate is relatively simple. First, you'll need to add the dependency in your `build.sbt`. Because Relate's releases are hosted with Sonatype, you'll also need to add Sonatype as one of the `resolvers`.

{% highlight scala %}
// build.sbt
libraryDependencies += "com.lucidchart" %% "relate" % "1.13.0"

resolvers += "Sonatype release repository" at "https://oss.sonatype.org/content/repositories/releases/"
{% endhighlight %}

## Step 2: Import All the Things!
Next, you'll need to import Relate before you can start [writing queries]({{site.baseurl}}/docs/writing-queries.html) or [retrieving data]({{site.baseurl}}/docs/retrieving-data.html). Additionally, you'll have to supply Relate with the `Connection` to use when making queries (this can be supplied implicitly).

{% highlight scala %}
import com.lucidchart.open.relate._
import com.lucidchart.open.relate.interp._
import java.sql.Connection

implicit val connection: Connection = ... // get a connection

// write awesome queries!
{% endhighlight %}

That's it! You're ready to move on and write queries!
