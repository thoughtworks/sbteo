scalaVersion := "2.10.4"

sbtPlugin := true

name := "sbteo"

organization := "com.thoughtworks"

libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.10.2"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.2.1" % "test"

libraryDependencies += "org.mashupbots.socko" % "socko-webserver_2.10" % "0.4.2"

libraryDependencies += "org.javassist" % "javassist" % "3.18.2-GA"

//releaseSettings

// publishing
//crossScalaVersions <<= sbtVersion {
//  case "0.12.4" => Seq("2.9.0", "2.9.1", "2.9.2", "2.9.3", "2.10.4", "2.11.1")
//  case "0.13.5" => Seq("2.10.4", "2.11.1")
//  case _ => sys.error("Unknown sbt version")
//}

