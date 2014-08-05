import sbt._
import Keys._
import scala.sys.process._

object ClientBuild extends Build {
  lazy val root =
    Project("root", file("."))
      .configs(IntegrationTest)
      .settings(Defaults.itSettings: _*)
      .settings(
        testOptions in IntegrationTest += Tests.Setup( () => {
          println("Make sure you're running these tests from this script: src/it/test.sh")
        })
      )
}
