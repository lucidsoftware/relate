import sbt._
import Keys._
import scala.sys.process._

object ClientBuild extends Build {
  lazy val root =
    Project("root", file("."))
      .configs(IntegrationTest)
      .settings(Defaults.itSettings: _*)
      .settings(libraryDependencies += specs)
      .settings(
        testOptions in IntegrationTest += Tests.Setup( () => {
          println("Make sure you're running these tests from this script: it-test.sh")
        })
      )

  lazy val specs = "org.specs2" %% "specs2" % "1.14" % "it"

}
