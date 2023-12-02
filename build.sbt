import sbt.Keys.scalacOptions

import java.net.URI


val BaseVersion = "0.4.0"

lazy val commonSettings = Seq(
  version := BaseVersion,
  startYear := Some(2017),
  organizationName := "Richard Hull",
  licenses += ("MIT", URI.create("https://opensource.org/licenses/MIT").toURL),
  homepage := Some(url("https://github.com/rm-hull/byok3")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/rm-hull/byok3"),
      "scm:git@github.com:rm-hull/byok3.git"
    )
  ),
  developers := List(
    Developer(id = "rhu",
      name = "Richard Hull",
      email = "rm_hull@yahoo.co.uk",
      url = url("http://www.destructuring-bind.org"))
  ),

  scalaVersion := "2.12.18",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions", "-Ypartial-unification"),

    // test dependencies
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.2.17" % Test,
    "org.scalacheck" %% "scalacheck" % "1.17.0" % Test
  )
)

lazy val core = (project in file("core"))
  .enablePlugins(BuildInfoPlugin, AutomateHeaderPlugin)
  .settings(
    commonSettings,
    name := "byok3-core",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.typelevel" %% "cats-core" % "2.10.0",
      "org.typelevel" %% "cats-effect" % "3.5.2",
      "org.parboiled" %% "parboiled" % "2.5.1"
    ),
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, "gitCommitHash" -> git.gitHeadCommit.value.getOrElse("Not Set")),
    buildInfoPackage := "byok3",
    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoOptions += BuildInfoOption.ToMap,
    buildInfoOptions += BuildInfoOption.ToJson,
    Test / fork := true)


lazy val repl = (project in file("repl"))
  .dependsOn(core)
  .enablePlugins(/*JavaAppPackaging,*/ AutomateHeaderPlugin)
  .settings(
    commonSettings,
    name := "byok3-repl",
    assembly / assemblyJarName := "byok3-repl.jar",
    Compile / mainClass := Some("byok3.console.REPL"),
    libraryDependencies ++= Seq(
      "org.jline" % "jline" % "3.24.1"
    )
  )

lazy val web = (project in file("web"))
  .dependsOn(core)
  .enablePlugins(SbtWeb, SbtTwirl, /*JavaAppPackaging,*/ AutomateHeaderPlugin)
  .settings(
    commonSettings,
    name := "byok3-web",
    assembly / assemblyJarName := "byok3-web.jar",
    run / fork := true,
    Compile / mainClass := Some("byok3.web.Server"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.5.0",
      "com.typesafe.akka" %% "akka-stream" % "2.8.0",
      "com.typesafe.akka" %% "akka-actor" % "2.8.0",
      "com.typesafe.akka" %% "akka-slf4j" % "2.8.0",
      "ch.qos.logback" % "logback-core" % "1.4.12"
    ),
    Assets / WebKeys.packagePrefix := "public/",
    Runtime / managedClasspath += (Assets / packageBin).value
  )


//enablePlugins(GitVersioning)
//
//val ReleaseTag = """^v([\d\.]+)$""".r
//
//git.baseVersion :=
//
//git.gitTagToVersionNumber := {
//  case ReleaseTag(version) => Some(version)
//  case _ => None
//}
//
//git.formattedShaVersion := {
//  val suffix = git.makeUncommittedSignifierSuffix(git.gitUncommittedChanges.value, git.uncommittedSignifier.value)
//
//  git.gitHeadCommit.value map { _.substring(0, 7) } map { sha =>
//    git.baseVersion.value + "-" + sha + suffix
//  }
//}