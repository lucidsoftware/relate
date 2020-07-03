import sbt.librarymanagement.CrossVersion

configs(Benchmark, Regression)

inConfig(Benchmark)(Defaults.testSettings)

inConfig(Regression)(Defaults.testSettings)

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-core" % "4.6.0" % Test,
  "org.specs2" %% "specs2-mock" % "4.6.0" % Test,
  "com.h2database" % "h2" % "1.4.191" % "test",
  "com.storm-enroute" %% "scalameter" % "0.19" % Benchmark,
  "com.storm-enroute" %% "scalameter" % "0.19" % Regression,
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.6"
)

libraryDependencies ++= (CrossVersion.binaryScalaVersion(scalaVersion.value) match {
  case "2.10" => Seq("com.typesafe.play" %% "anorm" % "2.4.0" % Benchmark)
  case "2.11" => Seq("com.typesafe.play" %% "anorm" % "2.5.2" % Benchmark)
  case "2.12" => Seq("com.typesafe.play" %% "anorm" % "2.6.0-M1" % Benchmark)
  case "2.13" => Seq("org.playframework.anorm" %% "anorm" % "2.6.7" % Benchmark)
})

testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework")

publishTo := sonatypePublishToBundle.value
