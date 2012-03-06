import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

	val appName = "grom-rest"
	val appVersion = "1.0-SNAPSHOT"

	val appDependencies = Seq(
		"com.amazonaws" % "aws-java-sdk" % "1.0.002",
		"org.scalatest" % "scalatest" % "1.7.1" % "test"
	)

	val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA)
		.settings ()

}
