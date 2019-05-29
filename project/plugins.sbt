resolvers += Classpaths.typesafeReleases

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.2.5")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.8")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.0.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.10")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.4.1")
addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.8")

//addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.5.6")