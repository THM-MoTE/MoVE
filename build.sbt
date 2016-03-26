unmanagedJars in Compile += Attributed.blank(file(System.getenv("JAVA_HOME") + "/jre/lib/ext/jfxrt.jar"))

fork := true

unmanagedResourceDirectories in Compile += baseDirectory.value / "conf"

scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation")

lazy val root = (project in file(".")).
  settings(
    organization := "thm",
    name := "move",
    version := "0.1",
    scalaVersion := "2.11.7",
    javacOptions ++= Seq("-source", "1.8")
    )

libraryDependencies ++= Seq(
    "com.novocode" % "junit-interface" % "0.11" % "test"
    )
