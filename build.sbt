import sbt.Keys.scalacOptions

val BaseVersion = "0.3.0"
scalaVersion := "2.12.4"
scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions", "-Ypartial-unification")

lazy val commonSettings = Seq(
  version := BaseVersion,
  startYear := Some(2017),
  organizationName := "Richard Hull",
  licenses += ("MIT", new URL("https://opensource.org/licenses/MIT")),
  homepage := Some(url("https://github.com/rm-hull/byok3")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/rm-hull/byok3"),
      "scm:git@github.com:rm-hull/byok3.git"
    )
  ),
  developers := List(
    Developer(id="rhu",
      name="Richard Hull",
      email="rm_hull@yahoo.co.uk",
      url=url("http://www.destructuring-bind.org"))
  ),

  // test dependencies
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    "org.scalacheck" %% "scalacheck" % "1.13.5" % Test
  )
)

lazy val core = (project in file("core"))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(
    commonSettings,
    name := "byok3-core",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "1.0.1",
      "org.typelevel" %% "cats-effect" % "0.9"
    )
  )

lazy val repl = (project in file("repl"))
  .dependsOn(core)
  .enablePlugins(JavaAppPackaging, AutomateHeaderPlugin)
  .settings(
    commonSettings,
    name := "byok3-repl",
    assemblyJarName in assembly := "byok3-repl.jar",
    mainClass in (Compile, run) := Some("byok3.console.REPL"),
    libraryDependencies ++= Seq(
      "org.jline" % "jline" % "3.6.1"
    )
  )

lazy val web = (project in file("web"))
  .dependsOn(core)
  .enablePlugins(SbtWeb, SbtTwirl, JavaAppPackaging, AutomateHeaderPlugin)
  .settings(
    commonSettings,
    name := "byok3-web",
    assemblyJarName in assembly := "byok3-web.jar",
    mainClass in (Compile, run) := Some("byok3.web.Server"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.1.0",
      "com.typesafe.akka" %% "akka-stream" % "2.5.11",
      "com.typesafe.akka" %% "akka-actor"  % "2.5.11",
      "com.typesafe.akka" %% "akka-slf4j"  % "2.5.11",
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    ),
    WebKeys.packagePrefix in Assets := "public/",
    managedClasspath in Runtime += (packageBin in Assets).value
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