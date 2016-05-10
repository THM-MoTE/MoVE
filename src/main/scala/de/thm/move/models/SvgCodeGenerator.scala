/**
  * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
  */

package de.thm.move.models

import java.io.ByteArrayOutputStream
import java.nio.file.{Files, Path}
import java.util.Locale
import javafx.embed.swing.SwingFXUtils
import javafx.scene.Node
import javafx.scene.paint._
import javafx.scene.shape.{LineTo, MoveTo, PathElement, QuadCurveTo}
import javax.imageio.ImageIO

import de.thm.move.Global._
import de.thm.move.util.GeometryUtils
import de.thm.move.util.GeometryUtils._
import de.thm.move.util.ResourceUtils
import de.thm.move.views.shapes._

import scala.xml.{Elem, Null, PrettyPrinter, UnprefixedAttribute}
import scala.collection.JavaConversions._

class SvgCodeGenerator {

  val lineWidth = 100
  val indentation = 2

  /** Converts the given color into a css-style color (e.g. rgb(255,0,0)) */
  def colorToCssColor(p: Paint): String = p match {
    case c: Color =>
      val red = (c.getRed * 255).toInt
      val green = (c.getGreen * 255).toInt
      val blue = (c.getBlue * 255).toInt
      s"rgb($red,$green,$blue)"
    case _ => throw new IllegalArgumentException(s"Can't generate color for: $p")
  }

  private def shapesWithIds(shapes:List[Node]) = shapes.zipWithIndex.map {
    case (shape,idx) => (shape, idx.toString)
  }

  def generateShapes(shapes:List[Node], width:Double, height:Double): Elem = {
    <svg
      width={width.toString}
      height={height.toString}
      xmlns="http://www.w3.org/2000/svg"
      xmlns:xlink="http://www.w3.org/1999/xlink"
      >
      <defs>
        { shapesWithIds(shapes).flatMap {
            case (shape:ColorizableShape, id) => generateGradient(shape, id)
            case _ => None
          }
        }
      </defs>
      {
        shapesWithIds(shapes).map {
          case (shape, id) => generateShape(shape, id)
        }
      }
    </svg>
  }
  def generateShape(shape:Node, id:String): Elem = shape match {
    case rect:ResizableRectangle => genRectangle(rect, id)
    case ellipse:ResizableCircle => genCircle(ellipse, id)
    case line:ResizableLine => genLine(line)
    case polygon:ResizablePolygon => genPolygon(polygon, id)
    case path:ResizablePath => genPath(path, id)
    case img:ResizableImage => genImage(img)
    case curvedPolygon:QuadCurvePolygon => genCurvedPolygon(curvedPolygon, id)
    case curvedPath:QuadCurvePath => genCurvedPath(curvedPath, id)
    case text:ResizableText => genText(text)
    case _ => throw new IllegalArgumentException(s"Can't generate svg code for: $shape")
  }

  def generatePrettyPrinted(shapes:List[Node], width:Double,height:Double): String = {
    val xml = generateShapes(shapes, width,height)
    val printer = new PrettyPrinter(lineWidth,indentation)
    printer.format(xml)
  }

  private def genRectangle(rectangle:ResizableRectangle, id:String): Elem = {
    <rect
      x={rectangle.getX.toString}
      y={rectangle.getY.toString}
      width={rectangle.getWidth.toString}
      height={rectangle.getHeight.toString}
      style={genColorStyle(rectangle)}
      stroke-dasharray = {rectangle.getStrokeDashArray.mkString(",")}
      /> %
      fillAttribute(rectangle, id) %
      fillOpacityAttribute(rectangle, id)  %
      transformationAttribute(rectangle)
  }

  private def genCircle(ellipse:ResizableCircle, id:String): Elem = {
    <ellipse
      cx={ellipse.getCenterX.toString}
      cy={ellipse.getCenterY.toString}
      rx={asRadius(ellipse.getWidth).toString}
      ry={asRadius(ellipse.getHeight).toString}
      style={genColorStyle(ellipse)}
      stroke-dasharray = {ellipse.getStrokeDashArray.mkString(",")}
      /> %
      fillAttribute(ellipse, id) %
      fillOpacityAttribute(ellipse, id)  %
      transformationAttribute(ellipse)
  }

  private def genLine(line:ResizableLine): Elem = {
    <line
      x1={line.getStartX.toString}
      y1={line.getStartY.toString}
      x2={line.getEndX.toString}
      y2={line.getEndY.toString}
      style={genColorStyle(line)}
      stroke-dasharray = {line.getStrokeDashArray.mkString(",")}
      /> %
      transformationAttribute(line)
  }

  private def genPolygon(polygon:ResizablePolygon, id:String): Elem = {
    <polygon
      points={
        polygon.getPoints.map(_.toInt).mkString(",")
      }
      style={genColorStyle(polygon)}
      stroke-dasharray = {polygon.getStrokeDashArray.mkString(",")}
      /> %
      fillAttribute(polygon, id) %
      fillOpacityAttribute(polygon, id)
  }

  private def genPath(path:ResizablePath, id:String): Elem = {
    <polyline
      points={
        path.getPoints.flatMap {
          case (x,y) => List(x,y)
        }.mkString(",")
      }
      style={genColorStyle(path)}
      stroke-dasharray = {path.getStrokeDashArray.mkString(",")}
      fill = "none"
      /> %
      transformationAttribute(path)
  }

  private def genCurveLike(pathlike:AbstractQuadCurveShape): Elem = {
    <path
      d={
        pathlike.getElements.map {
          case move:MoveTo => s"M ${move.getX} ${move.getY}"
          case line:LineTo => s"L ${line.getX} ${line.getY}"
          case curved:QuadCurveTo =>
            s"Q ${curved.getControlX} ${curved.getControlY} ${curved.getX} ${curved.getY}"
        }.mkString(" ")
      }
      style={genColorStyle(pathlike)}
      stroke-dasharray = {pathlike.getStrokeDashArray.mkString(",")}
      /> %
      transformationAttribute(pathlike)
  }

  private def genCurvedPath(curvedPath:QuadCurvePath, id:String): Elem = {
    genCurveLike(curvedPath) % new UnprefixedAttribute("fill", "none", Null)
  }

  private def genCurvedPolygon(curvedPolygon:QuadCurvePolygon, id:String): Elem = {
    genCurveLike(curvedPolygon)  %
     fillAttribute(curvedPolygon, id) %
     fillOpacityAttribute(curvedPolygon, id)  %
     transformationAttribute(curvedPolygon)
  }

  private def genImage(img:ResizableImage): Elem = {
    <image
      x={img.getX.toString}
      y={img.getY.toString}
      width={img.getWidth.toString}
      height={img.getHeight.toString}
      xlink:href={
        img.srcEither match {
          case Left(uri) =>
            uri.toString
          case Right(bytes) =>
            val byteStr = ResourceUtils.encodeBase64String(bytes)
            s"data:image/png;base64,$byteStr"
        }
      }
      /> %
      transformationAttribute(img)
  }

  private def genText(text:ResizableText): Elem = {
    <text
      x={text.getX.toString}
      y={text.getY.toString}
      fill={colorToCssColor(text.getFontColor)}
      style={ List(
          s"font-family: ${text.getFont.getFamily}",
          s"font-size: ${text.getSize.toInt}pt",
          s"font-weight: ${if(text.getBold) "bold" else "normal"}",
          s"font-style: ${if(text.getBold) "italic" else "normal"}",
          s"text-decoration: ${if(text.isUnderline) "underline" else "none"}"
        ).mkString(";")
      }
      >
      {text.getText}
    </text> % transformationAttribute(text)
  }

  private def fillAttribute(shape:ColorizableShape, id:String) = {
    val fill = shape.fillPatternProperty.get match {
      case FillPattern.None => "white"
      case FillPattern.Solid => colorToCssColor(shape.oldFillColorProperty.get)
      case _ => s"url(#$id)"
    }
    new UnprefixedAttribute("fill",fill,Null)
  }

  private def fillOpacityAttribute(shape:ColorizableShape, id:String) = {
    val opacity = shape.fillPatternProperty.get match {
      case FillPattern.None => "0.0"
      case _ => "%.2f".formatLocal(Locale.US, shape.oldFillColorProperty.get.getOpacity)
    }
    new UnprefixedAttribute("fill-opacity",opacity,Null)
  }

  private def transformationAttribute(node:Node) = {
    new UnprefixedAttribute("transform", generateRotation(node).getOrElse(""), Null)
  }

  private def generateRotation(node:Node): Option[String] = {
    val rotation = node.getRotate
    if(rotation == 0 | rotation == 360) None
    else {
      val degree = rotation.toInt
      val bounds = node.getBoundsInLocal
      val (x,y) = GeometryUtils.middleOfLine(bounds.getMinX, bounds.getMinY, bounds.getMaxX,bounds.getMaxY)
      Some(s"rotate($degree $x $y)")
    }
  }

  private def generateGradient(shape:Node with ColorizableShape, id:String):Option[Elem] = {
    shape.fillPatternProperty.get match {
      case FillPattern.VerticalCylinder =>
        Some(<linearGradient id={id.toString} x1="0%" y1="0%" x2="100%" y2="0%">
          <stop offset="0%" style={s"stop-color:${colorToCssColor(shape.getStrokeColor)};stop-opacity:1"}/>
          <stop offset="45%" style={s"stop-color:${colorToCssColor(shape.oldFillColorProperty.get)};stop-opacity:1"}/>
          <stop offset="55%" style={s"stop-color:${colorToCssColor(shape.oldFillColorProperty.get)};stop-opacity:1"}/>
          <stop offset="100%" style={s"stop-color:${colorToCssColor(shape.getStrokeColor)};stop-opacity:1"}/>
        </linearGradient>)
      case FillPattern.HorizontalCylinder =>
        Some(<linearGradient id={id.toString} x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" style={s"stop-color:${colorToCssColor(shape.getStrokeColor)};stop-opacity:1"}/>
          <stop offset="45%" style={s"stop-color:${colorToCssColor(shape.oldFillColorProperty.get)};stop-opacity:1"}/>
          <stop offset="55%" style={s"stop-color:${colorToCssColor(shape.oldFillColorProperty.get)};stop-opacity:1"}/>
          <stop offset="100%" style={s"stop-color:${colorToCssColor(shape.getStrokeColor)};stop-opacity:1"}/>
        </linearGradient>)
      case FillPattern.Sphere =>
       Some(<radialGradient id={id.toString} cx="50%" cy="50%" r="50%" fx="50%" fy="50%">
          <stop offset="0%" style={s"stop-color:${colorToCssColor(shape.oldFillColorProperty.get)}; stop-opacity:1"} />
          <stop offset="20%" style={s"stop-color:${colorToCssColor(shape.oldFillColorProperty.get)}; stop-opacity:1"} />
          <stop offset="100%" style={s"stop-color:${colorToCssColor(shape.getStrokeColor)};stop-opacity:1"} />
        </radialGradient>)
      case _ if shape.getFillColor.isInstanceOf[ImagePattern] =>
          //get the underlying image
        val imgpattern = shape.getFillColor.asInstanceOf[ImagePattern]
        val img = imgpattern.getImage
        val byteOutput = new ByteArrayOutputStream()
        ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", byteOutput) //write img into OutputStream
        val bytes = byteOutput.toByteArray //turn stream into Array[Byte]
        val byteStr = ResourceUtils.encodeBase64String(bytes)
        //add a little bit more px to fill the full smoothed polygons
        val width = shape.getBoundsInLocal.getWidth+5
        val height = shape.getBoundsInLocal.getHeight+5
        //finaly create a svg-pattern with an underlying base64-encoded image
        Some(
          <pattern id={id} patternUnits="objectBoundingBox" width="100" height="100">
            <image
              x="0"
              y="0"
              width={width.toString}
              height={height.toString}
              xlink:href={s"data:image/png;base64,$byteStr"}
              />
          </pattern>
        )
      case _ => None
    }
  }

  private def genColorStyle(shape:ColorizableShape): String = {
    List(
      "stroke: " + colorToCssColor(shape.getStrokeColor),
      "stroke-width: " + shape.getStrokeWidth.toInt
    ).mkString(";")
  }

  private def horizontalGradient(id:String, startColor:Color, endColor:Color): Elem = {
    <linearGradient id={id} x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" style={s"stop-color:${colorToCssColor(startColor)};stop-opacity:1"} />
      <stop offset="100%" style={s"stop-color:${colorToCssColor(endColor)};stop-opacity:1"} />
    </linearGradient>
  }

  private def verticalGradient(id:String, startColor:Color, endColor:Color): Elem = {
    <linearGradient id={id} x1="0%" y1="0%" x2="0%" y2="100%">
      <stop offset="0%" style={s"stop-color:${colorToCssColor(startColor)};stop-opacity:1"} />
      <stop offset="100%" style={s"stop-color:${colorToCssColor(endColor)};stop-opacity:1"} />
    </linearGradient>
  }

  private def radialGradient(id:String, startColor:Color, endColor:Color): Elem = {
    <radialGradient id={id} cx="50%" cy="50%" r="50%" fx="50%" fy="50%">
      <stop offset="0%" style={s"stop-color:${colorToCssColor(startColor)}; stop-opacity:0"} />
      <stop offset="100%" style={s"stop-color:${colorToCssColor(endColor)};stop-opacity:1"} />
    </radialGradient>
  }

  def writeToFile(str:String)(target:Path): Unit = {
    val writer = Files.newBufferedWriter(target, encoding)

    try {
      writer.write(str)
    } finally {
      writer.close()
    }
  }
}
