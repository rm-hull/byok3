lazy val commonSettings = Seq(
  version := "0.2.0",
  startYear := Some(2017),
  organizationName := "Richard Hull",
  licenses += ("MIT", new URL("https://opensource.org/licenses/MIT")),

  scalaVersion := "2.12.3",
  scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")
)

lazy val root = (project in file("."))
  .settings(
    commonSettings,
    name := "byok3",

    mainClass in (Compile, run) := Some("byok3.console.REPL"),

    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats" % "0.9.0",
      "org.typelevel" %% "cats-effect" % "0.3",
      "org.jline" % "jline" % "3.4.0",
      "com.typesafe.akka" %% "akka-http" % "10.0.10",
      "com.typesafe.akka" %% "akka-stream" % "2.4.19",
      "com.typesafe.akka" %% "akka-actor"  % "2.4.19",
      "com.typesafe.akka" %% "akka-slf4j"  % "2.4.19",
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    ),

    // test dependencies
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.4" % Test,
      "org.scalacheck" %% "scalacheck" % "1.13.5" % Test
    )
  )
  .enablePlugins(JavaAppPackaging)