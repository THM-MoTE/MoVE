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

  def newRectangle(point:Point, width:Double, height:Double)(fillColor:Color, strokeColor:Color):ResizableRectangle = {
    val rectangle = new ResizableRectangle(point, width, height)
    rectangle.colorizeShape(fillColor, strokeColor)
    rectangle
  }

  def newLine(start:Point, end:Point, strokeSize:Int)(fillColor:Color, strokeColor:Color):ResizableLine = {
    val line = new ResizableLine(start, end, strokeSize)
    line.colorizeShape(fillColor, strokeColor)
    line
  }

  def newCircle(point:Point, width:Double, height:Double)(fillColor:Color, strokeColor:Color):ResizableCircle = {
    val circle = new ResizableCircle(point, width, height)
    circle.colorizeShape(fillColor, strokeColor)
  }

  def newAnchor(point:Point):Anchor = {
    val (x,y) = point
    new Anchor(x,y)
  }

  def newPolygon(points:List[Point])(fillColor:Color, strokeColor:Color):ResizablePolygon = {
    val polygon = ResizablePolygon(points)
    polygon.colorizeShape(fillColor, strokeColor)
    polygon
  }
}
