package de.thm.move.models

import java.io.PrintWriter
import java.net.URI
import java.nio.charset.Charset
import java.nio.file.{Paths, Files}
import javafx.scene.Node
import javafx.scene.paint.{Paint, Color}

import de.thm.move.models.CommonTypes.Point
import de.thm.move.views.shapes._

object ModelicaCodeGenerator {
  type Lines = List[String]
  val encoding = Charset.forName("UTF-8")
  val generateAndWrite = (ModelicaCodeGenerator.writeToFile _).compose(ModelicaCodeGenerator.generate _)

  private def genOrigin(x:Double, y:Double): String =
    s"""origin={${x.toInt}, ${y.toInt}}"""

  private def genPoints(ps:List[Point]):String = {
    val psStrings = ps.map (genPoint(_)+",").mkString.dropRight(1)
    s"""points = {$psStrings}"""
  }

  private def genColor(name:String, p:Paint):String = p match {
    case c:Color =>
    s"""${name} = {${(c.getRed*255).toInt},${(c.getGreen*255).toInt},${(c.getBlue*255).toInt}}"""
    case _ => throw new IllegalArgumentException("Can't create rgb-values from non-color paint-values")
  }

  private def genPoint(p:Point):String = s"{${p._1.toInt},${p._2.toInt}}"

  def generateShape[A <: Node](shape:A): String = shape match {
    case rectangle:ResizableRectangle =>
      val origin = genOrigin(rectangle.getX, rectangle.getY)
      val strokeColor = genColor("lineColor", rectangle.getStrokeColor)
      val fillColor = genColor("fillColor", rectangle.getFillColor)
      val endTop = genPoint(rectangle.getTopRight)
      val endBottom = genPoint(rectangle.getBottomRight)

      s"""Rectangle(
         |${origin},
         |${strokeColor},
         |${fillColor},
         |fillPattern = FillPattern.Solid,
         |extend = {$endTop, $endBottom}
         |)
       """.stripMargin.dropRight(2)
    case circle:ResizableCircle => "NOT IMPLEMENTED"
    case line:ResizableLine =>
      val origin = genOrigin(line.getStartX, line.getStartY)
      val points = genPoints( List((line.getStartX, line.getStartY), (line.getEndX, line.getEndY)) )
      val color = genColor("color", line.getStrokeColor)
      val thickness = s"thickness = ${line.getStrokeWidth}"

      s"""Line(
         |${origin},
         |${points},
         |${color},
         |${thickness},
         |)
       """.stripMargin.dropRight(2)
  }

  def generate[A <: Node](shapes:List[A]): Lines = {
    shapes map generateShape
  }

  def writeToFile(lines:Lines)(target:URI): Unit = {
    val path = Paths.get(target)
    val writer = Files.newBufferedWriter(path, encoding)
    val filenamestr = path.getFileName.toString
    val modelName = if(filenamestr.endsWith(".mo")) filenamestr.dropRight(3) else filenamestr

    try {
      val header = generateHeader(modelName)
      val footer = generateFooter(modelName)
      val str = (header + lines.mkString("\n") + footer)
      writer.write(str)
      writer.write("\n")
    } finally {
      writer.close()
    }
  }

  private def generateHeader(modelName:String):String =
      s"model $modelName\n" +
      "annotation(\n"

  private def generateFooter(modelName:String):String =
      s");\nend $modelName;"

}
