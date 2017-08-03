name := "byok3"

version := "0.1.0"

scalaVersion := "2.12.3"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature")

mainClass in (Compile, run) := Some("byok3.REPL")
mainClass in (Compile, packageBin) := Some("byok3.REPL")

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats" % "0.9.0",
  "org.typelevel" %% "cats-effect" % "0.3",
  "org.jline" % "jline" % "3.3.0"
)

// test dependencies
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.3" % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.5" % "test"
)


