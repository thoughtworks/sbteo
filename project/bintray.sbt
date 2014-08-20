resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
  url(
    "http://dl.bintray.com/content/sbt/sbt-plugins-releases"))(
    Resolver.ivyStylePatterns)

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.2")