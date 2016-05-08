/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.models

import java.io.PrintWriter
import java.net.URI
import java.nio.charset.Charset
import java.nio.file.{Files, Paths}
import java.util.Base64
import javafx.scene.Node
import javafx.scene.paint.{Color, Paint}
import javafx.scene.shape.{LineTo, MoveTo}
import javafx.scene.text.TextAlignment
import javafx.geometry.Bounds

import de.thm.move.models.CommonTypes.Point
import de.thm.move.models.ModelicaCodeGenerator.FormatSrc
import de.thm.move.models.ModelicaCodeGenerator.FormatSrc.FormatSrc
import de.thm.move.util.PointUtils._
import de.thm.move.util.{Convertable, GeometryUtils, ResourceUtils}
import de.thm.move.util.Convertable._
import de.thm.move.views.shapes._

/** Codegenerator for modelica-source code.
  * @param srcFormat format of generated code; either Pretty or Oneline
  * @param pxPerMm Pixelcount per modelica "unit" a.k.a. millimeter
  * @param paneWidth width of the icon's coordinate system
  * @param paneHeight height of the icon's coordinate system
  */
class ModelicaCodeGenerator(
    srcFormat:FormatSrc,
    pxPerMm:Int,
    paneWidth:Double,
    paneHeight:Double) {
  type Lines = List[String]
  val encoding = Charset.forName("UTF-8")

  /** Converts v (in pixel) to modelica "units" */
  private def convertVal(v:Double):Double = v/pxPerMm
  private def convertPoint(p:Point):Point = p.map(convertVal)

  private def genOrigin(p:Point): String = genOrigin(p.x,p.y)
  private def genOrigin(x:Double, y:Double): String =
    s"""origin = ${genPoint(x,y)}"""

  private def genPoints(ps: Seq[Point]):String = {
    val psStrings = ps.map (genPoint).mkString(",")
    s"""points = {$psStrings}"""
  }

  private def genColor(name:String, p:Paint)(implicit ev:Convertable[Paint,String]):String =
    s"$name = ${ev.convert(p)}"

  private def genStrokeWidth(elem:ColorizableShape, key:String="lineThickness"): String =
    s"$key = ${elem.getStrokeWidth}"

  private def genPoint(p:Point):String = {
    val convP = convertPoint(p)
    s"{${convP.x.toInt},${convP.y.toInt}}"
  }
  private def genPoint(x:Double,y:Double):String = genPoint((x,y))

  private def genFillAndStroke(shape:ColorizableShape)(implicit indentIdx:Int):String = {
    val strokeColor = genColor("lineColor", shape.getStrokeColor)
    val fillColor = shape.oldFillColorProperty.get match {
      case null => genColor("fillColor", Color.WHITE)
      case _ => genColor("fillColor", shape.oldFillColorProperty.get)
    }
    val thickness = genStrokeWidth(shape)
    val linePattern = genLinePattern(shape)

    s"""${spaces}${strokeColor},
    |${spaces}${fillColor},
    |${spaces}${thickness},
    |${spaces}${linePattern}""".stripMargin.replaceAll("\n", linebreak)
  }

  private def genLinePattern(shape:ColorizableShape):String = {
    val linePattern = LinePattern.toString + "." + shape.linePattern.get.toString
    s"pattern = ${linePattern}"
  }

  private def genFillPattern(shape:ColorizableShape):String = {
    val fillPattern = FillPattern.toString + "." + shape.fillPatternProperty.get
    s"fillPattern = ${fillPattern}"
  }

  private def convertY(p:Point):Point = (p.x, paneHeight - p.y)
  private def convertYDistance(p:Point):Point = (p.x, p.y*(-1))

  private def genPosition(rectangle:RectangleLike): (Point,Point,Point) = {
    /* Because javafx y-axis go's from top (0px) to bottom (maxHeight px)
     * and modelicas y-axis go's from bottom to top we need to convert y-coordinates
     */
    val originP = GeometryUtils.middleOfLine(rectangle.getTopLeft, rectangle.getBottomRight)
    val extTop = rectangle.getTopLeft - originP
    val extBottom = rectangle.getBottomRight - originP
    (
      convertY(originP),
      convertYDistance(extTop),
      convertYDistance(extBottom)
    )
  }

  private def genPositionForPathLike(bounds:Bounds, ps:Seq[Point]): (Point, Seq[Point]) = {
    val minP = (bounds.getMinX,bounds.getMinY)
    val maxP = (bounds.getMaxX,bounds.getMaxY)
    val originP = GeometryUtils.middleOfLine(minP,maxP)
    val convertedPoints = ps.map { point =>
      convertYDistance(point - originP)
    }

    (
      convertY(originP),
      convertedPoints
    )
  }

  def generateShape[A <: Node]
    (shape:A, modelname:String, target:URI)(indentIdx:Int): String = shape match {
    case rectangle:ResizableRectangle => genRectangle(rectangle)(indentIdx)
    case circle:ResizableCircle => genCircle(circle)(indentIdx)
    case line:ResizableLine => genLine(line)(indentIdx)
    case path:ResizablePath => genPath(path)(indentIdx)
    case polygon:ResizablePolygon => genPolygon(polygon)(indentIdx)
    case curve:QuadCurvePolygon => genCurvedPolygon(curve)(indentIdx)
    case curvedL:QuadCurvePath => genCurvedPath(curvedL)(indentIdx)
    case resImg:ResizableImage => genImage(resImg, modelname, target)(indentIdx)
    case text:ResizableText => genText(text)(indentIdx)
    case _ => throw new IllegalArgumentException(s"Can't generate mdoelica code for: $shape")
  }


  private def genRectangle(rectangle:ResizableRectangle)(indentIdx:Int):String = {
    val (originP, extTop,extBottom) = genPosition(rectangle)
     val origin = genOrigin(originP)
     val ext1 = genPoint(extTop)
     val ext2 = genPoint(extBottom)
    val fillPattern = genFillPattern(rectangle)

    implicit val newIndentIdx = indentIdx + 2
    val colors = genFillAndStroke(rectangle)
    s"""${spaces(indentIdx)}Rectangle(
       |${spaces}${origin},
       |${colors},
       |${spaces}${fillPattern},
       |${spaces}extent = {$ext1, $ext2}
       |${spaces(indentIdx)})""".stripMargin.replaceAll("\n", linebreak)
 }

 private def genCircle(circle:ResizableCircle)(indentIdx:Int):String = {
   val angle = "endAngle = 360"
   val (originP, extTop,extBottom) = genPosition(circle)
    val origin = genOrigin(originP)
    val ext1 = genPoint(extTop)
    val ext2 = genPoint(extBottom)
   val fillPattern = genFillPattern(circle)
   implicit val newIndentIdx = indentIdx + 2
   val colors = genFillAndStroke(circle)
   s"""${spaces(indentIdx)}Ellipse(
       |${spaces}${origin},
       |${colors},
       |${spaces}${fillPattern},
       |${spaces}extent = {$ext1, $ext2},
       |${spaces}$angle
       |${spaces(indentIdx)})""".stripMargin.replaceAll("\n", linebreak)
 }

 private def genLine(line:ResizableLine)(indentIdx:Int):String = {
   val ps = List(
     (line.getStartX, line.getStartY),
     (line.getEndX, line.getEndY)
   )
   val (originP,pointList) = genPositionForPathLike(line.getBoundsInLocal, ps)
   val origin = genOrigin(originP)
   val points = genPoints( pointList )
   val color = genColor("color", line.getStrokeColor)
   val thickness = genStrokeWidth(line, "thickness")
   val linePattern = genLinePattern(line)

   implicit val newIndentIdx = indentIdx + 2

   s"""${spaces(indentIdx)}Line(
      |${spaces}${origin},
      |${spaces}${points},
      |${spaces}${color},
      |${spaces}${linePattern},
      |${spaces}${thickness}
      |${spaces(indentIdx)})""".stripMargin.replaceAll("\n", linebreak)
 }

 private def genPath(path:ResizablePath)(indentIdx:Int):String = {
   val ps = path.allElements.flatMap {
     case move:MoveTo =>
       val point = ( move.getX, move.getY )
       List( point )
     case line:LineTo =>
       val point = ( line.getX, line.getY )
       List( point )
   }
   val (originP, pointList) = genPositionForPathLike(path.getBoundsInLocal, ps)
   val origin = genOrigin(originP)
   val points = genPoints(pointList)
   val color = genColor("color", path.getStrokeColor)
   val thickness = genStrokeWidth(path, "thickness")
   val linePattern = genLinePattern(path)

   implicit val newIndentIdx = indentIdx + 2
   s"""${spaces(indentIdx)}Line(
      |${spaces}${origin},
      |${spaces}${points},
      |${spaces}${linePattern},
      |${spaces}${color},
      |${spaces}${thickness}
      |${spaces(indentIdx)})""".stripMargin.replaceAll("\n", linebreak)
 }

 private def genPolygon(polygon:ResizablePolygon)(indentIdx:Int):String = {
   val edgePoints = for {
     idx <- 0 until polygon.getPoints.size by 2
     x = polygon.getPoints.get(idx).toDouble
     y = polygon.getPoints.get(idx+1).toDouble
   } yield (x,y)
   val (originP, pointList) = genPositionForPathLike(polygon.getBoundsInLocal, edgePoints)
   val origin = genOrigin(originP)
   val points = genPoints(pointList)
   val fillPattern = genFillPattern(polygon)

   implicit val newIndentIdx = indentIdx + 2
   val colors = genFillAndStroke(polygon)

   s"""${spaces(indentIdx)}Polygon(
      |${spaces}${origin},
      |${spaces}${points},
      |${colors},
      |${spaces}${fillPattern}
      |${spaces(indentIdx)})""".stripMargin.replaceAll("\n", linebreak)
 }

 private def genCurvedPolygon(curve:QuadCurvePolygon)(indentIdx:Int):String = {
   val edgePoints = for(point <- curve.getUnderlyingPolygonPoints)
     yield (point.x, point.y)
   val (originP, pointList) = genPositionForPathLike(curve.getBoundsInLocal, edgePoints)
   val origin = genOrigin(originP)
   val points = genPoints(pointList)
   val fillPattern = genFillPattern(curve)

   implicit val newIndentIdx = indentIdx + 2
   val colors = genFillAndStroke(curve)

   s"""${spaces(indentIdx)}Polygon(
      |${spaces}${origin},
      |${spaces}${points},
      |${colors},
      |${spaces}${fillPattern},
      |${spaces}smooth = Smooth.Bezier
      |${spaces(indentIdx)})""".stripMargin.replaceAll("\n", linebreak)
 }
 private def genCurvedPath(curved:QuadCurvePath)(indentIdx:Int):String = {
   val edgePoints = for(point <- curved.getUnderlyingPolygonPoints)
     yield (point.x, point.y)
   val (originP, pointList) = genPositionForPathLike(curved.getBoundsInLocal, edgePoints)
   val origin = genOrigin(originP)
   val points = genPoints(pointList)
   val color = genColor("color", curved.getStrokeColor)
   val thickness = genStrokeWidth(curved, "thickness")
   val linePattern = genLinePattern(curved)

   implicit val newIndentIdx = indentIdx + 2

   s"""${spaces(indentIdx)}Line(
      |${spaces}${origin},
      |${spaces}${points},
      |${spaces}${color},
      |${spaces}${linePattern},
      |${spaces}${thickness},
      |${spaces}smooth = Smooth.Bezier
      |${spaces(indentIdx)})""".stripMargin.replaceAll("\n", linebreak)
 }
 private def genImage(img:ResizableImage, modelname:String, target:URI)(indentIdx:Int):String = {
   val (originP, extTop,extBottom) = genPosition(img)
    val origin = genOrigin(originP)
    val ext1 = genPoint(extTop)
    val ext2 = genPoint(extBottom)
   implicit val newIndentIdx = indentIdx + 2

   val imgStr = img.srcEither match {
     case Left(uri) =>
       copyImg(uri, target)
       val filename = ResourceUtils.getFilename(uri)
       s"""fileName = "modelica://$modelname/$filename""""
     case Right(bytes) =>
       val encoder = Base64.getEncoder
       val encodedBytes = encoder.encode(bytes)
       val byteStr = new String(encodedBytes, "UTF-8")
       s"""imageSource = "$byteStr""""
   }

   s"""${spaces(indentIdx)}Bitmap(
      |${spaces}${origin},
      |${spaces}extent = {${ext1}, ${ext2}},
      |${spaces}${imgStr}
      |${spaces(indentIdx)})""".stripMargin.replaceAll("\n", linebreak)
 }

 private def genText(text:ResizableText)(indentIdx:Int):String = {
   val bounding = text.getBoundsInLocal
   val topP = (bounding.getMinX, bounding.getMinY)
   val bottomP = (bounding.getMaxX, bounding.getMaxY)
   val originP = GeometryUtils.middleOfLine(topP,bottomP)
   val origin = genOrigin(convertY(originP))
   val start = genPoint(convertYDistance(topP - originP))
   val end = genPoint(convertYDistance(bottomP - originP))
   val str = text.getText
   val size = text.getSize
   val font = text.getFont
   val fontName = font.getFamily
   val styleList =
     List( if(text.getBold) Some("Bold") else None,
           if(text.getItalic) Some("Italic") else None,
           if(text.isUnderline) Some("Underline") else None ).flatten.map("TextStyle."+_)
   val color = genColor("textColor", text.getFontColor)
   val alignment = "TextAlignment." + (text.getTextAlignment match {
     case TextAlignment.LEFT => "Left"
     case TextAlignment.CENTER => "Center"
     case TextAlignment.RIGHT => "Right"
     case _ => throw new IllegalArgumentException("Can't generate TextAlignment for: "+
       text.getTextAlignment)
   })

   implicit val newIndentIdx = indentIdx + 2

   val style =
     if(styleList.isEmpty) ""
     else s"${spaces}textStyle = {" + styleList.mkString(",") + "},"

   s"""${spaces(indentIdx)}Text(
      |${spaces}${origin},
      |${spaces}extent = {${start},${end}},
      |${spaces}textString = "${str}",
      |${spaces}fontSize = ${size},
      |${spaces}fontName = "${fontName}",
      |${style}
      |${spaces}${color},
      |${spaces}horizontalAlignment = ${alignment}
      |${spaces(indentIdx)})""".stripMargin.replaceAll("\n", linebreak)
 }

  private def copyImg(src:URI, target:URI): Unit = {
    val targetPath = Paths.get(target).getParent
    val srcPath = Paths.get(src)
    val filename = srcPath.getFileName
    Files.copy(srcPath, targetPath.resolve(filename))
  }


  private def generateIcons[A <: Node](modelname:String, target:URI, shapes:List[A]): Lines = {
    val systemStartpoint = genPoint((0.0,0.0))
    val systemEndpoint = genPoint((paneWidth, paneHeight))

    val iconStr =
      s"""${spaces(2)}Icon (
      |${spaces(4)}coordinateSystem(
      |${spaces(6)}extent = {${systemStartpoint},$systemEndpoint}
      |${spaces(4)}),""".stripMargin.replaceAll("\n", linebreak)

    val graphicsStart = s"${spaces(4)}graphics = {"
    //generates a ,-separated list of Shapes
    //e.g.: Rectangle(..),Circle(..)
    val shapeStr = shapes.zipWithIndex.map {
      case (e,idx) if idx < shapes.length-1 =>
        generateShape(e, modelname, target)(6) + ","
      case (e,_) => generateShape(e, modelname, target)(6)
    }
    iconStr :: graphicsStart :: shapeStr ::: List(s"${spacesOrNothing(4)}})")
  }

  /** Generates a new model with the given name, source target and shapes */
  def generate[A <: Node](modelname:String, target:URI, shapes:List[A]): Lines = {
    val header = generateHeader(modelname)(2)
    val footer = generateFooter(modelname)(2)

    val graphics = generateIcons(modelname, target, shapes) ::: List(footer)
    header :: graphics
  }

  /** Generates an existing model and adds the new generated Icon */
  def generateExistingFile[A <: Node](modelname:String, target:URI, shapes:List[A]): Lines = {
    val graphics = generateIcons(modelname, target, shapes)
    graphics
  }

  def writeToFile(beforeIcons:String, lines:Lines, afterIcons:String)(target:URI): Unit = {
    val path = Paths.get(target)
    val writer = Files.newBufferedWriter(path, encoding)

    try {
      val str = lines.mkString(linebreakOrNothing)
      writer.write(beforeIcons + linebreakOrNothing)
      writer.write(str)
      writer.write(afterIcons)
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
