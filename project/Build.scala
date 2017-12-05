import sbt._
import Keys._
import scala.sys.process._

object Build extends AutoPlugin {

  override val trigger = allRequirements

  object autoImport {
    val Benchmark = config("bench").hide extend Test
    val Regression = config("regression").hide extend Benchmark
  }

}
