lazy val Benchmark = config("bench") extend Test

lazy val Regression = config("regression") extend Benchmark

lazy val relate = Project(
  "relate",
  file("."),
  settings = Defaults.coreDefaultSettings ++ Seq(
    name := "Relate",
    organization := "com.lucidchart",
    version := "1.13.0-SNAPSHOT",
    scalaVersion := "2.11.7",
    crossScalaVersions := Seq("2.10.6", "2.11.7"),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-language:higherKinds"
    ),
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2" % "2.3.12" % "test",
      "com.h2database" % "h2" % "1.4.191" % "test",
      "com.storm-enroute" %% "scalameter" % "0.7" % "bench",
      "com.storm-enroute" %% "scalameter" % "0.7" % "regression",
      "com.typesafe.play" %% "anorm" % "2.4.0" % "bench"
    ),
    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
    parallelExecution in Benchmark := false,
    parallelExecution in Regression := false,
    logBuffered := false,
    pgpPassphrase := Some(Array()),
    pgpPublicRing := file(System.getProperty("user.home")) / ".pgp" / "pubring",
    pgpSecretRing := file(System.getProperty("user.home")) / ".pgp" / "secring",
    pomExtra := (
      <url>https://github.com/lucidsoftware/relate</url>
      <licenses>
        <license>
        <name>Apache License</name>
        <url>http://www.apache.org/licenses/</url>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:lucidsoftware/relate.git</url>
        <connection>scm:git:git@github.com:lucidsoftware/relate.git</connection>
      </scm>
      <developers>
        <developer>
          <id>msiebert</id>
          <name>Mark Siebert</name>
        </developer>
        <developer>
          <id>gregghz</id>
          <name>Gregg Hernandez</name>
        </developer>
        <developer>
          <id>matthew-lucidchart</id>
          <name>Matthew Barlocker</name>
        </developer>
        <developer>
          <id>pauldraper</id>
          <name>Paul Draper</name>
        </developer>
      </developers>
    ),
    pomIncludeRepository := { _ => false },
    publishMavenStyle := true,
    credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", System.getenv("SONATYPE_USERNAME"), System.getenv("SONATYPE_PASSWORD")),
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    }
  )
).configs(Benchmark).settings(inConfig(Benchmark)(Defaults.testSettings): _*).
  configs(Regression).settings(inConfig(Regression)(Defaults.testSettings): _*)
