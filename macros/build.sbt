libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.3.3" % Test,
  "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.specs2" %% "specs2-core" % "4.6.0" % Test,
  "org.specs2" %% "specs2-mock" % "4.6.0" % Test,
)

moduleName := "relate-macros"

scalacOptions += "-language:experimental.macros"
publishTo := sonatypePublishToBundle.value
