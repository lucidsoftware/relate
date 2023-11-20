import sbt.librarymanagement.CrossVersion

configs(Benchmark, Regression)

inConfig(Benchmark)(Defaults.testSettings)

inConfig(Regression)(Defaults.testSettings)

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-core" % "5.4.0" % Test,
  ("org.scalamock" %% "scalamock" % "5.1.0" % Test).cross(CrossVersion.for3Use2_13),
  "com.h2database" % "h2" % "2.2.224" % "test",
)

libraryDependencies ++= (CrossVersion.binaryScalaVersion(scalaVersion.value) match {
  case "2.13" => Seq("org.playframework.anorm" %% "anorm" % "2.6.7" % Benchmark)
})

testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework")

publishTo := sonatypePublishToBundle.value
