publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { x => false }

pomExtra := <url>https://github.com/thoughtworks/sbteo</url>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
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