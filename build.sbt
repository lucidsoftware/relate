import com.lucidchart.sbtcross.ProjectAggregateArgument.toArgument

lazy val macros = project.in(file("macros")).cross.dependsOn(relate)
lazy val `macros2.13` = macros("2.13.12")
lazy val macrosAggregate = macros.aggregate( `macros2.13`).settings(
  publish / skip := true
)

lazy val relate = project.in(file("relate")).cross
lazy val `relate2.13` = relate("2.13.12")
lazy val relateAggregate = relate.aggregate(`relate2.13`).settings(
  publish / skip := true
)

lazy val postgres = project.in(file("postgres")).cross.dependsOn(relate)
lazy val `postgres2.13` = postgres("2.13.12")
lazy val postgresAggregate = postgres.aggregate(`postgres2.13`).settings(
  publish / skip := true
)

val benchmarkTag = Tags.Tag("benchmark")

inScope(Global)(Seq(
  concurrentRestrictions += Tags.exclusive(benchmarkTag),
  credentials += Credentials(
    "Sonatype Nexus Repository Manager",
    "oss.sonatype.org",
    sys.env.getOrElse("SONATYPE_USERNAME", ""),
    sys.env.getOrElse("SONATYPE_PASSWORD", "")
  ),
  organization := "com.lucidchart",
  PgpKeys.pgpPassphrase := Some(Array.emptyCharArray),
  developers ++= List(
    Developer("gregghz", "Gregg Hernandez", "", url("https://github.com/gregghz")),
    Developer("matthew-lucidchart", "Matthew Barlocker", "", url("https://github.com/matthew-lucidchart")),
    Developer("msiebert", "Mark Siebert", "", url("https://github.com/msiebert")),
    Developer("pauldraper", "Paul Draper", "", url("https://github.com/pauldraper"))
  ),
  homepage := Some(url("https://github.com/lucidsoftware/relate")),
  licenses += "Apache License 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"),
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-Werror"
  ),
  scmInfo := Some(ScmInfo(url("https://github.com/lucidsoftware/relate"), "scm:git:git@github.com:lucidsoftware/relate.git")),
  Benchmark / test / tags += benchmarkTag -> 1,
  Benchmark / testOnly / tags += benchmarkTag -> 1,
  Benchmark / testQuick / tags += benchmarkTag -> 1,
  version := sys.props.getOrElse("build.version", "0-SNAPSHOT")
))

publish / skip := true
publishTo := sonatypePublishToBundle.value
