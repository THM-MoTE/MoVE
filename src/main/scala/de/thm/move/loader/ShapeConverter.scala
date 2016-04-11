package de.thm.move.loader

import javafx.scene.paint.Color

import de.thm.move.models.CommonTypes._
import de.thm.move.models.{LinePattern, FillPattern}
import de.thm.move.util.GeometryUtils._
import de.thm.move.util.PointUtils._
import de.thm.move.views.shapes._
import de.thm.move.loader.parser.PropertyParser
import de.thm.move.loader.parser.ast._

class ShapeConverter(pxPerMm:Int, system:Point) {

  private def applyLineColor(shape:ResizableShape with ColorizableShape, color:Color, linePattern:String, strWidth:Double): Unit = {
    val lp = linePattern.split("\\.")(1)
    val linePatt = LinePattern.withName(lp)
    val cssClass = LinePattern.linePatternToCssClass(linePatt)
    shape.linePattern.setValue(linePatt)
    shape.getStyleClass().add(cssClass)
    shape.setStrokeWidth(strWidth)
  }

  private def applyColor(shape:ResizableShape with ColorizableShape, fs:FilledShape): Unit = {
    shape.setFillColor(fs.fillColor)
    shape.setStrokeColor(fs.strokeColor)

    val fillPatt = FillPattern.withName(fs.fillPattern.split("\\.")(1))
    shape.fillPatternProperty.setValue(fillPatt)

    shape.setFillColor(FillPattern.getFillColor(fillPatt, fs.fillColor, fs.strokeColor))
    applyLineColor(shape, fs.strokeColor, fs.strokePattern, fs.strokeSize)
  }

  private def convertPoint(p:Point):Point = {
    val (_,h) = system
    (p.x, h-p.y)
  }

  def getShapes(ast:ModelicaAst):List[ResizableShape] = ast match {
    case Model(_,xs) => xs flatMap getShapes
    case Icon(_,graphics) => graphics map getShape
    case _ => List()
  }

  def getShape(ast:ShapeElement): ResizableShape = ast match {
    case RectangleElement(gi,fs,bp,ext,rad) =>
      //TODO what happens if origin is defined? (extent = relative to origin)
      val (p1,p2) = ext
      val convStart = convertPoint(p1)
      val (w,h) = (p2 - p1).abs
      val rect = new ResizableRectangle(convStart, w,h)
      applyColor(rect, fs)
      rect.setVisible(gi.visible)
      rect
    case Ellipse(gi, fs, ext, _,_) =>
      val (p1,p2) = ext
      val convStart = convertPoint(p1)
      val (w,h) = (p2 - p1).abs.map(asRadius)
      val ellipse = new ResizableCircle(convStart, w,h)
      applyColor(ellipse, fs)
      ellipse.setVisible(gi.visible)
      ellipse
    case pe:PathElement if(pe.points.size == 2) =>
      val startP = convertPoint(pe.points.head)
      val endP = convertPoint(pe.points.tail.head)
      val line = new ResizableLine(startP,endP, pe.strokeSize.toInt)
      applyLineColor(line, pe.color, pe.strokePattern, pe.strokeSize)
      line.setVisible(pe.gItem.visible)
      line
    case pe:PathElement =>
      val points = pe.points.map(convertPoint)
      val path = ResizablePath(points)
      path.setStrokeColor(pe.color)
      path.setStrokeWidth(pe.strokeSize)
      path.setVisible(pe.gItem.visible)
      applyLineColor(path, pe.color, pe.strokePattern, pe.strokeSize)

      if(pe.smooth == "Smooth.Bezier") QuadCurvePath(path)
      else path
    case Polygon(gi,fs,ps,smooth) =>
      val points = ps.map(convertPoint)
      val polygon = ResizablePolygon(points)
      applyColor(polygon, fs)
      polygon.setVisible(gi.visible)
      polygon
  }
}

object ShapeConverter {
  def gettCoordinateSystemSizes(ast:ModelicaAst):List[Point] = ast match {
    case Model(_,xs) => xs flatMap {
      case Icon(Some(system),_) =>
        val (p1,p2) = system.extension
        List((
          Math.abs(p1.x - p2.x),
          Math.abs(p1.y - p2.y)
        ))
      case Icon(None,_) =>
      val (x,y) = PropertyParser.defaultCoordinateSystemSize
      List( (x, y) )
      case _ => Nil
    }
  }
}
