import java.lang.System
import java.io.File

//include javafx-jar (from java's home directory) in classpath
unmanagedJars in Compile += Attributed.blank(file(System.getenv("JAVA_HOME") + "/jre/lib/ext/jfxrt.jar"))

//fork a process when runnign so that javafx doesn't crash
fork := true

//include ./conf in classpath
unmanagedResourceDirectories in Compile += baseDirectory.value / "conf"

scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature"
    )

lazy val copyRscs = taskKey[Unit]("Copies needed resources to resource-directory.")

lazy val rscFiles = settingKey[Seq[File]]("The files that get copied with copyRscs task")

lazy val rscCopyTarget = settingKey[File]("The target directory for copyRscs task")

lazy val moveConfigDir = settingKey[File]("The config directory of move")

lazy val cleanConfig = taskKey[Unit]("Cleans user's config directory of move")

rscFiles := Seq(baseDirectory.value / "LICENSE")

moveConfigDir := new File(System.getProperty("user.home") + "/.move")

rscCopyTarget := (classDirectory in Compile).value

copyRscs := rscFiles.value.map { file =>
    IO.copyFile(file, rscCopyTarget.value / file.getName)
}

cleanConfig := IO.delete(moveConfigDir.value)

//append copyRscs-task to compile-task
compile <<= (compile in Compile) dependsOn copyRscs

lazy val root = (project in file(".")).
  settings(
    organization := "thm",
    name := "move",
    version := "0.6.0",
    scalaVersion := "2.11.7",
    javacOptions ++= Seq("-source", "1.8")
    )

mainClass in assembly := Some("de.thm.move.MoveApp")
assemblyJarName in assembly := s"${name.value}-${version.value}.jar"
test in assembly := {} //skip test's during packaging

libraryDependencies ++= Seq(
    "com.novocode" % "junit-interface" % "0.11" % "test",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
    "org.scala-lang.modules" %% "scala-xml" % "1.0.2"
    )
