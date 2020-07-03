import com.lucidchart.sbtcross.ProjectAggregateArgument.toArgument

lazy val macros = project.in(file("macros")).cross.dependsOn(relate)
lazy val `macros2.11` = macros("2.11.12")
lazy val `macros2.12` = macros("2.12.11")
lazy val `macros2.13` = macros("2.13.2")
lazy val macrosAggregate = macros.aggregate(`macros2.11`, `macros2.12`, `macros2.13`).settings(
  skip in publish := true
)

lazy val relate = project.in(file("relate")).cross
lazy val `relate2.11` = relate("2.11.12")
lazy val `relate2.12` = relate("2.12.11")
lazy val `relate2.13` = relate("2.13.2")
lazy val relateAggregate = relate.aggregate(`relate2.11`, `relate2.12`, `relate2.13`).settings(
  skip in publish := true
)

lazy val postgres = project.in(file("postgres")).cross.dependsOn(relate)
lazy val `postgres2.11` = postgres("2.11.12")
lazy val `postgres2.12` = postgres("2.12.11")
lazy val `postgres2.13` = postgres("2.13.2")
lazy val postgresAggregate = postgres.aggregate(`postgres2.11`, `postgres2.12`, `postgres2.13`).settings(
  skip in publish := true
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
    "-feature"
  ),
  scmInfo := Some(ScmInfo(url("https://github.com/lucidsoftware/relate"), "scm:git:git@github.com:lucidsoftware/relate.git")),
  tags in (Benchmark, test) += benchmarkTag -> 1,
  tags in (Benchmark, testOnly) += benchmarkTag -> 1,
  tags in (Benchmark, testQuick) += benchmarkTag -> 1,
  version := sys.props.getOrElse("build.version", "0-SNAPSHOT")
))

skip in publish := true
publishTo := sonatypePublishToBundle.value
