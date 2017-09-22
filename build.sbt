name := "byok3"
version := "0.1.0"
startYear := Some(2017)
organizationName := "Richard Hull"
licenses += ("MIT", new URL("https://opensource.org/licenses/MIT"))

scalaVersion := "2.12.3"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")
mainClass in (Compile, run) := Some("byok3.console.REPL")

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats" % "0.9.0",
  "org.typelevel" %% "cats-effect" % "0.3",
  "org.jline" % "jline" % "3.4.0",
  "com.typesafe.akka" %% "akka-http" % "10.0.10",
  "com.typesafe.akka" %% "akka-stream" % "2.5.4",
  "com.typesafe.akka" %% "akka-actor"  % "2.5.4",
  "com.typesafe.akka" %% "akka-slf4j"  % "2.5.4",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)

// test dependencies
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.5" % "test"
)

enablePlugins(JavaAppPackaging)


