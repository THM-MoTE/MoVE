fork := true

lazy val root = (project in file(".")).
  settings(
    organization := "thm",
    name := "move",
    version := "0.1",
    scalaVersion := "2.11.7",
    javacOptions ++= Seq("-source", "1.8")
    )
