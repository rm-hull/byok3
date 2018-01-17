resolvers += Classpaths.typesafeReleases

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.2.2")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "3.0.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.2")
//addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.0")
//addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "1.1")
//addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.3.13")
addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.6")

//addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.5.6")