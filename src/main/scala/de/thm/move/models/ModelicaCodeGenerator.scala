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

  private def genPoints(ps: Seq[Point]):String = {
    val psStrings = ps.map (genPoint(_)+",").mkString.dropRight(1)
    s"""points = {$psStrings}"""
  }

  private def genColor(name:String, p:Paint):String = p match {
    case c:Color =>
    s"""${name} = {${(c.getRed*255).toInt},${(c.getGreen*255).toInt},${(c.getBlue*255).toInt}}"""
    case _ => throw new IllegalArgumentException("Can't create rgb-values from non-color paint-values")
  }

  private def genStrokeWidth(elem:ColorizableShape, key:String="lineThickness"): String =
    s"$key = ${elem.getStrokeWidth}"

  private def genPoint(p:Point):String = s"{${p._1.toInt},${p._2.toInt}}"

  def generateShape[A <: Node](shape:A): String = shape match {
    case rectangle:ResizableRectangle =>
      val strokeColor = genColor("lineColor", rectangle.getStrokeColor)
      val fillColor = genColor("fillColor", rectangle.getFillColor)
      val thickness = genStrokeWidth(rectangle)

      val newY = paneHeight - rectangle.getY
      val endY = newY - rectangle.getHeight
      val endBottom = genPoint(rectangle.getBottomRight._1, endY)
      val start = genPoint(rectangle.getX, newY)

      s"""Rectangle(
         |${strokeColor},
         |${fillColor},
         |fillPattern = FillPattern.Solid,
         |${thickness},
         |extent = {$start, $endBottom}
         |)""".stripMargin
    case circle:ResizableCircle =>
      val angle = "endAngle = 360"
      val strokeColor = genColor("lineColor", circle.getStrokeColor)
      val fillColor = genColor("fillColor", circle.getFillColor)
      val thickness = genStrokeWidth(circle)

      val bounding = circle.getBoundsInLocal
      val newY = paneHeight - bounding.getMinY
      val endY = newY - bounding.getHeight
      val start = genPoint(bounding.getMinX, newY)
      val end = genPoint(bounding.getMaxX, endY)

      println(s"Boundingbox: top ${bounding.getMinX}, ${bounding.getMinY}")
      println(s"bottom ${bounding.getMaxX}, ${bounding.getMaxY}")

      s"""Ellipse(
          |${strokeColor},
          |${fillColor},
          |fillPattern = FillPattern.Solid,
          |${thickness},
          |extent = {$start,$end},
          |$angle
          |)""".stripMargin
    case line:ResizableLine =>
      //offset, if element was moved (=0 if not moved)
      val offsetX = line.getLayoutX
      val offsetY = line.getLayoutY
      val pointList = List(
        (line.getStartX + offsetX, paneHeight - (line.getStartY + offsetY)),
        (line.getEndX + offsetX, paneHeight - (line.getEndY + offsetY))
      )
      val points = genPoints( pointList )
      val color = genColor("color", line.getStrokeColor)
      val thickness = genStrokeWidth(line, "thickness")

      s"""Line(
         |${points},
         |${color},
         |${thickness}
         |)""".stripMargin

    case polygon:ResizablePolygon =>
      //offset, if element was moved (=0 if not moved)
      val offsetX = polygon.getLayoutX
      val offsetY = polygon.getLayoutY
      val edgePoints = for {
        idx <- 0 until polygon.getPoints.size by 2
        x = polygon.getPoints.get(idx).toDouble
        y = polygon.getPoints.get(idx+1).toDouble
      } yield (x+offsetX,paneHeight-(y+offsetY))

      val points = genPoints(edgePoints)
      val strokeColor = genColor("lineColor", polygon.getStrokeColor)
      val fillColor = genColor("fillColor", polygon.getFillColor)
      val thickness = genStrokeWidth(polygon)

      s"""Polygon(
         |${points},
         |${strokeColor},
         |${fillColor},
         |fillPattern = FillPattern.Solid,
         |${thickness}
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
