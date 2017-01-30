import sbt.cross.CrossVersionUtil

lazy val Benchmark = config("bench") extend Test

lazy val Regression = config("regression") extend Benchmark

lazy val buildSettings = Seq(
  organization := "com.lucidchart",
  organizationHomepage := Some(url("https://golucid.co")),
  organizationName := "Lucid Software",
  version := sys.props.getOrElse("build.version", "0-SNAPSHOT"),
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.1"),
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-language:higherKinds"
  )
)

inScope(Global)(buildSettings)

lazy val publishingSettings = Seq(
  pgpPassphrase := Some(Array()),
  pomExtra := (
    <licenses>
      <license>
      <name>Apache License</name>
      <url>http://www.apache.org/licenses/</url>
      </license>
    </licenses>
  ),
  pomIncludeRepository := { _ => false },
  publishMavenStyle := true,
  credentials += Credentials(
    "Sonatype Nexus Repository Manager",
    "oss.sonatype.org",
    sys.env.getOrElse("SONATYPE_USERNAME", ""),
    sys.env.getOrElse("SONATYPE_PASSWORD", "")
  ),
  developers += Developer("msiebert", "Mark Siebert", "", url("https://github.com/msiebert")),
  developers += Developer("gregghz", "Gregg Hernandez", "greggory.hz@gmail.com", url("https://github.com/gregghz")),
  developers += Developer("matthew-lucidchart", "Matthew Barlocker", "", url("https://github.com/matthew-lucidchart")),
  developers += Developer("pauldraper", "Paul Draper", "pauldraper@gmail.com", url("https://github.com/pauldraper")),
  scmInfo := Some(ScmInfo(
    url("https://github.com/lucidsoftware/relate"),
    "scm:git:git@github.com:lucidsoftware/relate.git"
  )),
  homepage := Some(url("https://github.com/lucidsoftware/relate")),
  publishTo <<= version { (v: String) =>
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }
)

lazy val macroSettings = buildSettings ++ Seq(
  moduleName := "relate-macros",
  scalacOptions += "-language:experimental.macros",
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _),
  libraryDependencies ++= Seq(
    "com.chuusai" %% "shapeless" % "2.3.2" % "test",
    "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
    "org.specs2" %% "specs2-core" % "3.8.7" % "test",
    "org.specs2" %% "specs2-mock" % "3.8.7" % "test",
    "org.typelevel" %% "macro-compat" % "1.1.1"
  )
)

lazy val macros = project.in(file("macros"))
  .settings(publishingSettings)
  .settings(macroSettings)
  .dependsOn(relate)

lazy val relate = project.in(file("relate"))
  .settings(publishingSettings)
  .settings(Defaults.coreDefaultSettings)
  .settings(buildSettings)
  .settings(
    name := "Relate",
    moduleName := "relate",
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2-core" % "3.8.7" % "test",
      "org.specs2" %% "specs2-mock" % "3.8.7" % "test",
      "com.h2database" % "h2" % "1.4.191" % "test",
      "com.storm-enroute" %% "scalameter" % "0.8.2" % "bench",
      "com.storm-enroute" %% "scalameter" % "0.8.2" % "regression"
    ),
    libraryDependencies ++= (CrossVersionUtil.binaryScalaVersion(scalaVersion.value) match {
      case "2.10" => Seq("com.typesafe.play" %% "anorm" % "2.4.0" % "bench")
      case "2.11" => Seq("com.typesafe.play" %% "anorm" % "2.5.2" % "bench")
      case "2.12" => Nil // note: can't run benchmarks for 2.12 until a suitable anorm artifact is avaiable
    }),
    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
    parallelExecution in Benchmark := false,
    parallelExecution in Regression := false,
    logBuffered := false
  )
  .configs(Benchmark).settings(inConfig(Benchmark)(Defaults.testSettings): _*)
  .configs(Regression).settings(inConfig(Regression)(Defaults.testSettings): _*)

lazy val root = project.in(file(".")).aggregate(relate, macros)