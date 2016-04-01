package de.thm.move.models

import java.io.PrintWriter
import java.net.URI
import java.nio.charset.Charset
import java.nio.file.{Paths, Files}
import javafx.scene.Node
import javafx.scene.paint.{Paint, Color}

import de.thm.move.models.CommonTypes.Point
import de.thm.move.models.ModelicaCodeGenerator.FormatSrc
import de.thm.move.models.ModelicaCodeGenerator.FormatSrc.FormatSrc
import de.thm.move.models.ModelicaCodeGenerator.FormatSrc.FormatSrc
import de.thm.move.util.ResourceUtils

import de.thm.move.views.shapes._

class ModelicaCodeGenerator(srcFormat:FormatSrc, paneWidth:Double, paneHeight:Double) {
  type Lines = List[String]
  val encoding = Charset.forName("UTF-8")

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

  def generateShape[A <: Node](shape:A, modelname:String, target:URI)(indentIdx:Int): String = shape match {
    case rectangle:ResizableRectangle =>
      val strokeColor = genColor("lineColor", rectangle.getStrokeColor)
      val fillColor = genColor("fillColor", rectangle.getFillColor)
      val thickness = genStrokeWidth(rectangle)

      val newY = paneHeight - rectangle.getY
      val endY = newY - rectangle.getHeight
      val endBottom = genPoint(rectangle.getBottomRight._1, endY)
      val start = genPoint(rectangle.getX, newY)

      implicit val newIndentIdx = indentIdx + 2

      s"""${spaces(indentIdx)}Rectangle(
         |${spaces}${strokeColor},
         |${spaces}${fillColor},
         |${spaces}fillPattern = FillPattern.Solid,
         |${spaces}${thickness},
         |${spaces}extent = {$start, $endBottom}
         |${spaces(indentIdx)})""".stripMargin.replaceAll("\n", linebreak)
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

      implicit val newIndentIdx = indentIdx + 2

      s"""${spaces(indentIdx)}Ellipse(
          |${spaces}${strokeColor},
          |${spaces}${fillColor},
          |${spaces}fillPattern = FillPattern.Solid,
          |${spaces}${thickness},
          |${spaces}extent = {$start,$end},
          |${spaces}$angle
          |${spaces(indentIdx)})""".stripMargin.replaceAll("\n", linebreak)
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

      implicit val newIndentIdx = indentIdx + 2

      s"""${spaces(indentIdx)}Line(
         |${spaces}${points},
         |${spaces}${color},
         |${spaces}${thickness}
         |${spaces(indentIdx)})""".stripMargin.replaceAll("\n", linebreak)

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

      implicit val newIndentIdx = indentIdx + 2

      s"""${spaces(indentIdx)}Polygon(
         |${spaces}${points},
         |${spaces}${strokeColor},
         |${spaces}${fillColor},
         |${spaces}fillPattern = FillPattern.Solid,
         |${spaces}${thickness}
         |${spaces(indentIdx)})""".stripMargin.replaceAll("\n", linebreak)

    case img:ResizableImage =>
      copyImg(img.uri, target)
      val filename = ResourceUtils.getFilename(img.uri)
      val uri = s"modelica://$modelname/$filename"

      val bounding = img.getBoundsInLocal
      val newY = paneHeight - img.getY
      val endY = newY - bounding.getHeight
      val start = genPoint(img.getX, newY)
      val end = genPoint(bounding.getWidth, endY)

      implicit val newIndentIdx = indentIdx + 2

      s"""${spaces(indentIdx)}Bitmap(
         |${spaces}extent = {${start}, ${end}},
         |${spaces}fileName "$uri"
         |${spaces(indentIdx)})""".stripMargin.replaceAll("\n", linebreak)
  }


  private def copyImg(src:URI, target:URI): Unit = {
    val targetPath = Paths.get(target).getParent
    val srcPath = Paths.get(src)
    val filename = srcPath.getFileName
    Files.copy(srcPath, targetPath.resolve(filename))
  }

  def generate[A <: Node](modelname:String, target:URI, shapes:List[A]): Lines = {
    val systemStartpoint = genPoint((0.0,0.0))
    val systemEndpoint = genPoint((paneWidth, paneHeight))

    val iconStr =
      s"""${spaces(2)}Icon (
         |${spaces(4)}coordinateSystem(
         |${spaces(6)}extent = {${systemStartpoint},$systemEndpoint}
         |${spaces(4)}),""".stripMargin.replaceAll("\n", linebreak)

    val header = generateHeader(modelname)(2)
    val footer = generateFooter(modelname)(2)

    val graphicsStart = s"${spaces(4)}graphics = {"
    val shapeStr = shapes.zipWithIndex.map {
      case (e,idx) if idx < shapes.length-1 =>
        generateShape(e, modelname, target)(6) + ","
      case (e,_) => generateShape(e, modelname, target)(6)
    }
    val graphics = graphicsStart :: shapeStr ::: List(s"${spacesOrNothing(4)}})", footer)
    header :: iconStr :: graphics
  }

  def writeToFile(lines:Lines)(target:URI): Unit = {
    val path = Paths.get(target)
    val writer = Files.newBufferedWriter(path, encoding)

    try {
      val str = lines.mkString(linebreakOrNothing)
      writer.write(str)
      writer.write("\n")
    } finally {
      writer.close()
    }
  }

  private def generateHeader(modelName:String)(implicit indentIdx:Int):String =
      s"model $modelName\n" +
        spacesOrNothing + "annotation("

  private def generateFooter(modelName:String)(implicit indentIdx:Int):String =
      spaces + ");\n" +
        s"end $modelName;"

  private def spacesOrNothing(implicit indent:Int): String = srcFormat match {
    case FormatSrc.Pretty => spaces
    case FormatSrc.Oneline => ""
  }

  private def spaces(implicit indent:Int): String = srcFormat match {
    case FormatSrc.Pretty => (for(_ <- 0 until indent) yield " ").mkString("")
    case FormatSrc.Oneline => " "
  }

  private def linebreak: String = srcFormat match {
    case FormatSrc.Oneline => " "
    case _ => "\n"
  }

  private def linebreakOrNothing:String = srcFormat match {
    case FormatSrc.Oneline => ""
    case _ => "\n"
  }
}


object ModelicaCodeGenerator {
  object FormatSrc extends Enumeration {
    type FormatSrc = Value
    val Oneline, Pretty = Value
  }
}