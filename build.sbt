scalaVersion := "2.10.4"

sbtPlugin := true

name := "sbteo"

organization := "com.thoughtworks"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % "2.10.2",
  "org.mashupbots.socko" % "socko-webserver_2.10" % "0.4.2",
  "org.javassist" % "javassist" % "3.18.2-GA",
  "net.liftweb" %% "lift-json" % "2.5.1"
)

resolvers += "com.github.sprsquish" at "https://raw.github.com/sprsquish/mvn-repo/master"

libraryDependencies ++= Seq(
  "com.github.sprsquish" %% "finagle-websockets" % "6.8.1" % "test",
  "org.specs2" %% "specs2" % "2.4" % "test"
)
//releaseSettings

// publishing
//crossScalaVersions <<= sbtVersion {
//  case "0.12.4" => Seq("2.9.0", "2.9.1", "2.9.2", "2.9.3", "2.10.4", "2.11.1")
//  case "0.13.5" => Seq("2.10.4", "2.11.1")
//  case _ => sys.error("Unknown sbt version")
//}

