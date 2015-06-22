import sbt._
import Keys._
import scala.sys.process._

object ClientBuild extends Build {
  lazy val root =
    Project("root", file("."))
      .configs(IntegrationTest)
      .settings(Defaults.itSettings: _*)
}
