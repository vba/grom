import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

	val appName = "grom-rest"
	val appVersion = "1.0-SNAPSHOT"

	val appDependencies = Seq(
		"com.amazonaws" % "aws-java-sdk" % "1.0.002",
		"org.mockito" % "mockito-core" % "1.9.0",
		"org.specs2" %% "specs2" % "1.8.2" % "test",
		"eu.medsea.mimeutil" % "mime-util" % "2.1.3",
		"org.openoffice" % "juh" % "3.2.1",
		"org.openoffice" % "ridl" % "3.2.1",
		"org.openoffice" % "unoil" % "3.2.1",
		"org.json" % "json" % "20090211",
		"commons-io" % "commons-io" % "20030203.000550"
	)


	val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings ()
}
