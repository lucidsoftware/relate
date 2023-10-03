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
)

libraryDependencies ++= (CrossVersion.binaryScalaVersion(scalaVersion.value) match {
  case "2.13" => Seq("org.playframework.anorm" %% "anorm" % "2.6.7" % Benchmark)
})

testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework")

publishTo := sonatypePublishToBundle.value
