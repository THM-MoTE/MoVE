/**
  * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
  */

package de.thm.move.models

import java.nio.file.{Files, Path}
import java.util.Locale
import javafx.scene.Node
import javafx.scene.paint.{Color, Paint}

import de.thm.move.Global._
import de.thm.move.views.shapes._

import scala.xml.Elem
import scala.collection.JavaConversions._

class SvgCodeGenerator {

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
    <svg width={width.toString} height={height.toString}>
      <defs>
        { shapesWithIds(shapes).flatMap {
            case (shape:ColorizableShape, id) => generateGradient(shape, id)
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
  def generateShape(shape:Node): Elem = shape match {
    case rect:ResizableRectangle => genRectangle(rect)
    case _ => throw new IllegalArgumentException(s"Can't generate svg code for: $shape")
  }

  private def genRectangle(rectangle:ResizableRectangle): Elem = {
    <rect
      x={rectangle.getX.toString}
      y={rectangle.getY.toString}
      width={rectangle.getWidth.toString}
      height={rectangle.getHeight.toString}
      />
  }

  private def generateGradient(shape:ColorizableShape, id:String):Option[Elem] = {
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
