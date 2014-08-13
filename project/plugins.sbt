
addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.4")

libraryDependencies <+= sbtVersion("org.scala-sbt" % "scripted-plugin" % _)

