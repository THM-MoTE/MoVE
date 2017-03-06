/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.loader

import java.io.ByteArrayInputStream
import java.nio.file.Path
import java.util.Base64
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.text.{Font, TextAlignment}
import javafx.scene.transform.Transform

import de.thm.move.controllers.factorys.ShapeFactory
import de.thm.move.loader.parser.PropertyParser
import de.thm.move.loader.parser.ast._
import de.thm.move.models.{FillPattern, LinePattern}
import de.thm.move.util.GeometryUtils
import de.thm.move.util.GeometryUtils._
import de.thm.move.types._
import de.thm.move.views.shapes._

/** Converts the parsed AST into "real" ResizableShapes.
  *
  * @param pxPerMm describes how many px are 1 unit from modelica
  *                Example: pxPerMm = 2 => every unit gets doubled
  * @param system (low,high) the coordinate system from the source file
  * @param srcFilePath the path to the parsed modelica-file
  */
class ShapeConverter(pxPerMm:Int, system:Extent, srcFilePath:Path) {

  type ErrorMsg = String

  lazy val parentPath = srcFilePath.getParent
  private val (low, high) = system
  private val xDistance = (0 - low.x).abs
  val translation = Transform.translate(xDistance, high.y*(-1))

  /** Converst the rotation-value.
    * Modelica rotates counter-clockwise; JavafX rotates clockwise
    */
  private val convertRotation: Double => Double = _*(-1)

  private def applyLineColor(
    shape:ResizableShape with ColorizableShape,
    color:Color,
    linePattern:String,
    strWidth:Double): Unit = {
    val lp = linePattern.split("\\.")(1)
    val linePatt = LinePattern.withName(lp)
    val cssClass = LinePattern.linePatternToCssClass(linePatt)
    shape.linePattern.setValue(linePatt)
    shape.getStyleClass().add(cssClass)
    shape.setStrokeColor(color)
    shape.setStrokeWidth(strWidth)
  }

  private def applyColor(shape:ResizableShape with ColorizableShape, fs:FilledShape): Unit = {
    shape.setFillColor(fs.fillColor)

    val fillPatt = FillPattern.withName(fs.fillPattern.split("\\.")(1))
    shape.fillPatternProperty.setValue(fillPatt)

    val width = shape.getBoundsInLocal.getWidth()
    val height = shape.getBoundsInLocal.getHeight()
    shape.setFillColor(FillPattern.getFillColor(fillPatt, fs.fillColor, fs.strokeColor, width, height))
    applyLineColor(shape, fs.strokeColor, fs.strokePattern, fs.strokeSize)
  }

  private def convertPoint(p:Point):Point = {
    val point2D = translation.transform(p.x,p.y)
    (point2D.getX, point2D.getY)
  }

  private def rectangleLikeDimensions(origin:Point, ext:Extent):(Point,Double,Double) = {
    val (p1,p2) = ext
    val convP1 = convertPoint(p1+origin)
    val convP2 = convertPoint(p2+origin)
    val (w,h) = (convP2 - convP1).abs
    (convP1, w,h)
  }

  def getShapes(ast:ModelicaAst):List[(ResizableShape, Option[ErrorMsg])] = ast match {
    case Model(_,icons) => getShapes(icons)
    case Icon(_,graphics, _,_) => graphics map getShape
    case _ => List()
  }

  def getShape(ast:ShapeElement): (ResizableShape, Option[ErrorMsg]) = ast match {
    case RectangleElement(gi,fs,bp,ext,rad) =>
      val (start,w,h) = rectangleLikeDimensions(gi.origin, ext)
      val rect = new ResizableRectangle(start, w,h)
      applyColor(rect, fs)
      rect.setVisible(gi.visible)
      rect.setRotate(convertRotation(gi.rotation))
      (rect, getError(ast, "Rectangle"))
    case Ellipse(gi, fs, ext, _,_) =>
      val (start,w,h) = rectangleLikeDimensions(gi.origin, ext)
      val middle = GeometryUtils.middleOfLine(start, (w+start.x,h+start.y) )
      val ellipse = new ResizableCircle(middle, asRadius(w),asRadius(h))
      applyColor(ellipse, fs)
      ellipse.setVisible(gi.visible)
      ellipse.setRotate(convertRotation(gi.rotation))
      (ellipse, getError(ast, "Ellipse"))
    case pe:PathElement if(pe.points.size == 2) =>
      val startP = convertPoint(pe.points.head+pe.gItem.origin)
      val endP = convertPoint(pe.points.tail.head+pe.gItem.origin)
      val line = new ResizableLine(startP,endP, pe.strokeSize.toInt)
      applyLineColor(line, pe.color, pe.strokePattern, pe.strokeSize)
      line.setVisible(pe.gItem.visible)
      line.setRotate(convertRotation(pe.gItem.rotation))
      (line, getError(ast, "Line"))
    case pe:PathElement =>
      val points = pe.points.map(x => convertPoint(x+pe.gItem.origin))
      val path =
        if(pe.smooth == "Smooth.Bezier") QuadCurvePath(points)
        else ResizablePath(points)
      path.setVisible(pe.gItem.visible)
      path.setRotate(convertRotation(pe.gItem.rotation))
      applyLineColor(path, pe.color, pe.strokePattern, pe.strokeSize)
      (path, getError(ast, "Line"))
    case Polygon(gi,fs,ps,smooth) =>
      val points = ps.map(x => convertPoint(x+gi.origin))
      val polygon =
        if(smooth == "Smooth.Bezier") new QuadCurvePolygon(points)
        else ResizablePolygon(points)
      applyColor(polygon, fs)
      polygon.setVisible(gi.visible)
      polygon.setRotate(convertRotation(gi.rotation))
      (polygon, getError(ast, "Polygon"))
    case ImageURI(gi, ext, uriStr) =>
      val imageName = uriStr.substring(uriStr.lastIndexOf("/")+1, uriStr.length)
      val uri = parentPath.resolve(imageName).toUri
      val img = ShapeFactory.newImage(uri)

      val (start,w,h) = rectangleLikeDimensions(gi.origin, ext)
      img.setXY(start)
      img.setWidth(w)
      img.setHeight(h)
      img.setVisible(gi.visible)
      img.setRotate(convertRotation(gi.rotation))
      (img, getError(ast, "Bitmap"))
    case ImageBase64(gi,ext,encodedStr) =>
      val decoder = Base64.getDecoder()
      val bytes = decoder.decode(encodedStr)
      val byteArrayStream = new ByteArrayInputStream(bytes)
      val img = new Image(byteArrayStream)

      val resizableImg = ResizableImage(bytes, img)
      val (start,w,h) = rectangleLikeDimensions(gi.origin, ext)
      resizableImg.setXY(start)
      resizableImg.setWidth(w)
      resizableImg.setHeight(h)
      resizableImg.setVisible(gi.visible)
      resizableImg.setRotate(convertRotation(gi.rotation))
      (resizableImg, getError(ast, "Bitmap"))
    case txt:Text =>
      val (x,y) = convertPoint(txt.extent._1+txt.gItem.origin)
      val fontSize = if(txt.size == 0.0) 12.0 else txt.size
      val font = Font.font(txt.fontName, fontSize)
      val text = new ResizableText(txt.text,x,y,font)
      text.setFontColor(txt.color)
      txt.style.foreach {
        case "TextStyle.Bold" => text.setBold(true)
        case "TextStyle.Italic" => text.setItalic(true)
        case "TextStyle.Underline" => text.setUnderline(true)
        case _ => //ignore
      }
      val txtAlign = TextAlignment.valueOf(txt.hAlignment.substring(txt.hAlignment.indexOf(".")+1).toUpperCase)
      text.setTextAlignment(txtAlign)
      text.setVisible(txt.gItem.visible)
      text.setRotate(convertRotation(txt.gItem.rotation))
      (text, getError(ast, "Text"))
    case a:Any => throw new IllegalArgumentException(s"Unknown shape-ast $a")
  }


  def getError(ast:ShapeElement, shapename:String): Option[ErrorMsg] =
    ast.warnings.foldLeft(None:Option[ErrorMsg]) {
      case (Some(acc), msg) => Some(acc + "\n  " + msg)
      case (None, msg) => Some(
        s"""$shapename: \n  $msg""")
    }
}

object ShapeConverter {
/*
  @deprecated("Will be removed; Use 'gettCoordinateSystem' instead", "0.7.2.X")
  def getSystemSize(iconOpt:Annotation):Option[Point] =
    iconOpt match {
      case Icon(Some(system),_,_,_) =>
        val (p1,p2) = system.extension
        Some((p1-p2).abs)
      case _ => None
    }

  @deprecated("Will be removed; Use 'gettCoordinateSystem' instead", "0.7.2.X")
  def gettCoordinateSystemSizes(ast:ModelicaAst):Point = ast match {
    case Model(name, iconOpt) =>
      getSystemSize(iconOpt).getOrElse(PropertyParser.defaultCoordinateSystemSize)
    case _ => throw new IllegalArgumentException("ast != Model")
  }
*/
  def getCoordinateSystem(iconOpt:Annotation):Option[Extent] =
    iconOpt match {
      case Icon(Some(system),_,_,_) =>
        Some(system.extension)
      case _ => None
    }

  def getCoordinateSystem(ast:ModelicaAst): Extent = ast match {
    case Model(name, iconOpt) =>
      getCoordinateSystem(iconOpt).getOrElse(PropertyParser.defaultCoordinateSystem)
    case _ => throw new IllegalArgumentException("ast != Model")
  }
}
