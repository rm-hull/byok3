resolvers += Classpaths.typesafeReleases

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.9")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.1.5")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.11.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.10.0")
//addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.4")
addSbtPlugin("com.github.sbt" % "sbt-git" % "2.0.1")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.11.1")

addSbtPlugin("org.playframework.twirl" % "sbt-twirl" % "2.0.2")
addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.2.0")

//addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.5.6")