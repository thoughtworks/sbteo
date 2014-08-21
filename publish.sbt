publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { x => false }

bintrayPublishSettings

bintray.Keys.repository  := "sbt-plugins"

bintray.Keys.bintrayOrganization := None

licenses  += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

pomExtra := 
  <scm>
    <url>git@github.com:thoughtworks/sbteo.git</url>
    <connection>scm:git:git@github.com:thoughtworks/sbteo.git</connection>
  </scm>
  <developers>
    <developer>
      <id>PilchardFriendly</id>
      <name>Nick Drew</name>
      <url>http://github.com/PilchardFriendly</url>
    </developer>
  </developers>
