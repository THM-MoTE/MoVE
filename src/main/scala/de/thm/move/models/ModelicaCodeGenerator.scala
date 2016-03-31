package de.thm.move.models

import java.io.PrintWriter
import java.net.URI
import java.nio.charset.Charset
import java.nio.file.{Paths, Files}
import javafx.scene.Node
import javafx.scene.paint.{Paint, Color}

import de.thm.move.models.CommonTypes.Point
import de.thm.move.views.shapes._

class ModelicaCodeGenerator(paneWidth:Double, paneHeight:Double) {
  type Lines = List[String]
  val encoding = Charset.forName("UTF-8")
  val generateAndWrite = (writeToFile _).compose(generate)

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
      val strokeColor = genColor("lineColor", rectangle.getStrokeColor)
      val fillColor = genColor("fillColor", rectangle.getFillColor)

      val newY = paneHeight - rectangle.getY
      val endY = newY - rectangle.getHeight
      val endBottom = genPoint(rectangle.getBottomRight._1, endY)
      val start = genPoint(rectangle.getX, newY)

      s"""Rectangle(
         |${strokeColor},
         |${fillColor},
         |fillPattern = FillPattern.Solid,
         |extent = {$start, $endBottom}
         |)""".stripMargin
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
         |)""".stripMargin
  }

  def generate[A <: Node](shapes:List[A]): Lines = {
    val graphicsStart = "graphics = {"
    val shapeStr = shapes.zipWithIndex.map {
      case (e,idx) if idx < shapes.length-1 =>
        generateShape(e) + ","
      case (e,_) => generateShape(e)
    }
    graphicsStart :: shapeStr ::: List("}")
  }

  def writeToFile(lines:Lines)(target:URI): Unit = {
    val path = Paths.get(target)
    val writer = Files.newBufferedWriter(path, encoding)
    val filenamestr = path.getFileName.toString
    val modelName = if(filenamestr.endsWith(".mo")) filenamestr.dropRight(3) else filenamestr

    val systemStartpoint = genPoint((0.0,0.0))
    val systemEndpoint = genPoint((paneWidth, paneHeight))

    try {
      val header = generateHeader(modelName)
      val iconStr =
        s"""Icon (
          |coordinateSystem(extent = {${systemStartpoint},$systemEndpoint}),
        """.stripMargin
      val footer = generateFooter(modelName)
      val str = (header + iconStr +lines.mkString("\n") +")"+ footer)
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
