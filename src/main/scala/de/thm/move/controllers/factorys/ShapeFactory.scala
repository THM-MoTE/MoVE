package de.thm.move.controllers.factorys

import javafx.scene.image.Image
import javafx.scene.paint.Color

import de.thm.move.models.CommonTypes._
import de.thm.move.views.Anchor
import de.thm.move.views.shapes._

object ShapeFactory {
  def newImage(img:Image):ResizableImage = {
    new ResizableImage(img)
  }

  private def setDefaultColor[T <: ColorizableShape](shape:T)(fillColor:Color, strokeColor:Color, strokeWidth:Int): T = {
    shape.colorizeShape(fillColor, strokeColor)
    shape.setStrokeWidth(strokeWidth)
    shape
  }

  def newRectangle(point:Point, width:Double, height:Double): (Color, Color, Int) => ResizableRectangle = {
    val rectangle = new ResizableRectangle(point, width, height)
    (setDefaultColor(rectangle) _)
  }

  def newLine(start:Point, end:Point, strokeSize:Int): (Color, Color, Int) => ResizableLine = {
    val line = new ResizableLine(start, end, strokeSize)
    (setDefaultColor(line) _)
  }

  def newCircle(point:Point, width:Double, height:Double): (Color, Color, Int) => ResizableCircle = {
    val circle = new ResizableCircle(point, width, height)
    (setDefaultColor(circle) _)
  }

  def newAnchor(point:Point):Anchor = {
    val (x,y) = point
    new Anchor(x,y)
  }

  def newPolygon(points:List[Point]): (Color, Color, Int) => ResizablePolygon = {
    val polygon = ResizablePolygon(points)
    (setDefaultColor(polygon) _)
  }
}
