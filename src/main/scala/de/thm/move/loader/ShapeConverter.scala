/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.loader

import java.io.ByteArrayInputStream
import java.util.Base64
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import java.net.URI
import java.nio.file.Paths

import de.thm.move.controllers.factorys.ShapeFactory
import de.thm.move.models.CommonTypes._
import de.thm.move.models.{LinePattern, FillPattern}
import de.thm.move.util.GeometryUtils._
import de.thm.move.util.PointUtils._
import de.thm.move.views.shapes._
import de.thm.move.loader.parser.PropertyParser
import de.thm.move.loader.parser.ast._
import java.nio.file.Path
import de.thm.move.util.GeometryUtils

/** Converts the parsed AST into "real" ResizableShapes.
  * @param pxPerMm describes how many px are 1 unit from modelica
  *                Example: pxPerMm = 2 => every unit gets doubled
  * @param system (width,height) from the coordinate system in which the shapes get loaded/displayed
  * @param srcFilePath the path to the parsed modelica-file
  */
class ShapeConverter(pxPerMm:Int, system:Point, srcFilePath:Path) {

  lazy val parentPath = srcFilePath.getParent

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
    val (_,h) = system
    (pxPerMm * p.x, pxPerMm*(h-p.y))
  }

  private def rectangleLikeDimensions(origin:Point, ext:Extent):(Point,Double,Double) = {
    val (p1,p2) = ext
    val convP1 = convertPoint(p1+origin)
    val convP2 = convertPoint(p2+origin)
    val (w,h) = (convP2 - convP1).abs
    (convP1, w,h)
  }

  def getShapes(ast:ModelicaAst):List[ResizableShape] = ast match {
    case Model(_,icons) => getShapes(icons)
    case Icon(_,graphics, _,_) => graphics map getShape
    case _ => List()
  }

  def getShape(ast:ShapeElement): ResizableShape = ast match {
    case RectangleElement(gi,fs,bp,ext,rad) =>
      val (start,w,h) = rectangleLikeDimensions(gi.origin, ext)
      val rect = new ResizableRectangle(start, w,h)
      applyColor(rect, fs)
      rect.setVisible(gi.visible)
      rect.setRotate(gi.rotation)
      rect
    case Ellipse(gi, fs, ext, _,_) =>
      val (start,w,h) = rectangleLikeDimensions(gi.origin, ext)
      val middle = GeometryUtils.middleOfLine(start, (w+start.x,h+start.y) )
      val ellipse = new ResizableCircle(middle, asRadius(w),asRadius(h))
      applyColor(ellipse, fs)
      ellipse.setVisible(gi.visible)
      ellipse.setRotate(gi.rotation)
      ellipse
    case pe:PathElement if(pe.points.size == 2) =>
      val startP = convertPoint(pe.points.head+pe.gItem.origin)
      val endP = convertPoint(pe.points.tail.head+pe.gItem.origin)
      val line = new ResizableLine(startP,endP, pe.strokeSize.toInt)
      applyLineColor(line, pe.color, pe.strokePattern, pe.strokeSize)
      line.setVisible(pe.gItem.visible)
      line.setRotate(pe.gItem.rotation)
      line
    case pe:PathElement =>
      val points = pe.points.map(x => convertPoint(x+pe.gItem.origin))
      val path =
        if(pe.smooth == "Smooth.Bezier") QuadCurvePath(points)
        else ResizablePath(points)
      path.setVisible(pe.gItem.visible)
      path.setRotate(pe.gItem.rotation)
      applyLineColor(path, pe.color, pe.strokePattern, pe.strokeSize)
      path
    case Polygon(gi,fs,ps,smooth) =>
      val points = ps.map(x => convertPoint(x+gi.origin))
      val polygon =
        if(smooth == "Smooth.Bezier") new QuadCurvePolygon(points)
        else ResizablePolygon(points)
      applyColor(polygon, fs)
      polygon.setVisible(gi.visible)
      polygon.setRotate(gi.rotation)
      polygon
    case ImageURI(gi, ext, uriStr) =>
      val imageName = uriStr.substring(uriStr.lastIndexOf("/")+1, uriStr.length)
      val uri = parentPath.resolve(imageName).toUri
      val img = ShapeFactory.newImage(uri)

      val (start,w,h) = rectangleLikeDimensions(gi.origin, ext)
      img.setXY(start)
      img.setWidth(w)
      img.setHeight(h)
      img.setVisible(gi.visible)
      img.setRotate(gi.rotation)
      img
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
      resizableImg.setRotate(gi.rotation)
      resizableImg
    case txt:Text =>
      val (x,y) = convertPoint(txt.extent._1+txt.gItem.origin)
      val font = Font.font(txt.fontName, txt.size)
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
      text.setRotate(txt.gItem.rotation)
      text
    case a:Any => throw new IllegalArgumentException(s"Unknown shape-ast $a")
  }
}

object ShapeConverter {

  def getSystemSize(iconOpt:Annotation):Option[Point] =
    iconOpt match {
      case Icon(Some(system),_,_,_) =>
        val (p1,p2) = system.extension
        Some((p1-p2).abs)
      case _ => None
    }

  def gettCoordinateSystemSizes(ast:ModelicaAst):Point = ast match {
    case Model(name, iconOpt) =>
      getSystemSize(iconOpt).getOrElse(PropertyParser.defaultCoordinateSystemSize)
    case _ => throw new IllegalArgumentException("ast != Model")
  }
}
