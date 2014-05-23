name := "Relate"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.4"

exportJars := true

exportJars in Test := false

autoScalaLibrary := true

retrieveManaged := true

libraryDependencies += "org.specs2" %% "specs2" % "2.3.12" % "test"

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
)

pomIncludeRepository := { _ => false }

useGpg := true

pgpReadOnly := false

publishMavenStyle := true

credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", System.getenv("SONATYPE_USERNAME"), System.getenv("SONATYPE_PASSWORD"))

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
