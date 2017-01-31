addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.3.2" % Test,
  "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.specs2" %% "specs2-core" % "3.8.7" % Test,
  "org.specs2" %% "specs2-mock" % "3.8.7" % Test,
  "org.typelevel" %% "macro-compat" % "1.1.1"
)

moduleName := "relate-macros"

scalacOptions += "-language:experimental.macros"
