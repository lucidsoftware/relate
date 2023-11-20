ThisBuild / scalaVersion := "2.13.12"

lazy val macros = project.in(file("macros")).dependsOn(relate.jvm("2.13.12")).settings(
  publish / skip := true
)

lazy val relate = (projectMatrix in file("relate")).settings(
  scalaVersion := "3.3.1",
  publish / skip := true,
  libraryDependencies ++= Seq(
    "org.specs2" %% "specs2-core" % "5.4.0" % Test,
    // use scalamock instead once it supports scala 3: https://github.com/paulbutcher/ScalaMock/issues/429
    //("org.scalamock" %% "scalamock" % "5.1.0" % Test).cross(CrossVersion.for3Use2_13),
    "org.mockito" % "mockito-core" % "5.7.0" % Test,
    "com.h2database" % "h2" % "2.2.224" % Test,
  )
).jvmPlatform(scalaVersions = Seq("3.3.1", "2.13.12"))

lazy val postgres = project.in(file("postgres")).dependsOn(relate.jvm("2.13.12")).settings(
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
    "-language:implicitConversions",
    //"-Werror"
  ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((3, _)) => Seq(
      "-source:3.0-migration",
    )
    case _ => Seq(
      "-Xfatal-warnings",
      "-Wunused:imports,privates,locals",
      //"-Wvalue-discard",
    )
  }),
  scmInfo := Some(ScmInfo(url("https://github.com/lucidsoftware/relate"), "scm:git:git@github.com:lucidsoftware/relate.git")),
  Benchmark / test / tags += benchmarkTag -> 1,
  Benchmark / testOnly / tags += benchmarkTag -> 1,
  Benchmark / testQuick / tags += benchmarkTag -> 1,
  version := sys.props.getOrElse("build.version", "0-SNAPSHOT")
))

publish / skip := true
publishTo := sonatypePublishToBundle.value
