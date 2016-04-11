package de.thm.move.loader

import javafx.scene.paint.Color

import de.thm.move.models.CommonTypes._
import de.thm.move.models.{LinePattern, FillPattern}
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

    val fillPatt = FillPattern.withName(fs.fillPattern)
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
      ???
    case pe:PathElement if(pe.points.size == 2) =>
      val startP = convertPoint(pe.points.head)
      val endP = convertPoint(pe.points.tail.head)
      val line = new ResizableLine(startP,endP, pe.strokeSize.toInt)
      applyLineColor(line, pe.color, pe.strokePattern, pe.strokeSize)
      line
    case pe:PathElement =>
      val points = pe.points.map(convertPoint)
      val path = ResizablePath(points)
      path.setStrokeColor(pe.color)
      path.setStrokeWidth(pe.strokeSize)
      applyLineColor(path, pe.color, pe.strokePattern, pe.strokeSize)
      if(pe.smooth == "Smooth.Bezier") QuadCurvePath(path)
      else path
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
