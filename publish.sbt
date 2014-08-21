publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { x => false }

bintrayPublishSettings

bintray.Keys.repository in bintray.Keys.bintray := "sbt-plugins"

bintray.Keys.bintrayOrganization in bintray.Keys.bintray := None

licenses  += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))
