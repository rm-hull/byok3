name := "byok3"

version := "0.1.0"

scalaVersion := "2.12.2"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies += "org.typelevel" %% "cats" % "0.9.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "0.3"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.3" % "test"
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.5" % "test"


