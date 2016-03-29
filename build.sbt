unmanagedJars in Compile += Attributed.blank(file(System.getenv("JAVA_HOME") + "/jre/lib/ext/jfxrt.jar"))

fork := true

unmanagedResourceDirectories in Compile += baseDirectory.value / "conf"

scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation")

lazy val copyRscs = taskKey[Unit]("Copies needed resources to resource-directory.")

lazy val rscFiles = settingKey[Seq[File]]("The files that get copied with copyRscs task")

lazy val rscCopyTarget = settingKey[File]("The target directory for copyRscs task")

rscFiles := Seq(baseDirectory.value / "LICENSE")

rscCopyTarget := (classDirectory in Compile).value

copyRscs := rscFiles.value.map { file =>
    IO.copyFile(file, rscCopyTarget.value / file.getName)
}

//append copyRscs-task to compile-task
compile <<= (compile in Compile) dependsOn copyRscs

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
