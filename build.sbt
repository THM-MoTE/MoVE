import java.lang.System

resolvers += "Sonatype releases" at "https://oss.sonatype.org/content/repositories/snapshots"

//include javafx-jar (from java's home directory) in classpath
unmanagedJars in Compile += {
  val jhome = Resources.getJavaHome
  Resources.checkExists(jhome / "lib" / "ext" / "jfxrt.jar")
}

//fork a process when runnign so that javafx doesn't crash
fork := true

//include ./conf in classpath
unmanagedResourceDirectories in Compile += baseDirectory.value / "conf"

scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature"
    )

lazy val copyright = "(c) 2016 Nicola Justus"
lazy val licenseName = "MPL V2.0 <http://mozilla.org/MPL/2.0>"

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
compile in Compile <<= (compile in Compile) dependsOn copyRscs

sourceGenerators in Compile <+= Def.task {
  val dir:File = (sourceManaged in Compile).value
  InfoGenerator.generateProjectInfo(dir, Seq(
    "name" -> (name in root).value,
    "version" -> (version in root).value,
    "organization" -> (organization in root).value,
    "copyright" -> copyright,
    "licenseName" -> licenseName))
}

lazy val root = (project in file(".")).
  settings(
    organization := "de.thm.mote",
    name := "Move",
    version := "0.7.0",
    scalaVersion := "2.11.8",
    javacOptions ++= Seq("-source", "1.8")
    ).
  dependsOn(RootProject(file("../recently")))

mainClass in Compile := Some("de.thm.move.MoveApp")
assemblyJarName in assembly := s"${name.value}-${version.value}.jar"
test in assembly := {} //skip test's during packaging
assemblyExcludedJars in assembly := {
  val cp = (fullClasspath in assembly).value
  cp filter {_.data.getName == "jfxrt.jar"}
}


libraryDependencies ++= Seq(
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
    "org.scala-lang.modules" %% "scala-xml" % "1.0.4",
    "org.scalatest" % "scalatest_2.11" % "3.0.0" % "test",
    "org.reactfx" % "reactfx" % "2.0-SNAPSHOT")
