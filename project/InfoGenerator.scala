import sbt._

object InfoGenerator {
  def generateProjectInfo(dir:File,
    values:Seq[(String, String)],
    `package`:String = "build",
    className:String = "ProjectInfo"):Seq[File] = {
    val file = dir / `package` / (className+".scala")
    val generatedValues = values.map {
      case (name, value) => s"""lazy val $name="$value""""
    }.mkString("\n")
    val content =
      s"""package ${`package`}
      |object $className {
      |$generatedValues
      |}
      """.stripMargin

    IO.write(file, content)
    Seq(file)
  }
}
