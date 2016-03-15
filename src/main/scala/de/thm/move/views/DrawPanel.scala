package de.thm.move.views

import javafx.scene.Cursor
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape._

import de.thm.move.models.CommonTypes._

class DrawPanel extends Pane {
  private var shapes = List[Shape]()

  super.setStyle("-fx-background-color:black")

  this.setMaxWidth(Double.MaxValue)
  this.setMaxHeight(Double.MaxValue)
  super.getChildren.add(new Label("lökajsdflökjasdf"))

  def drawShape(s:Shape):Unit = {
    super.getChildren.add(s)
    shapes = s :: shapes
  }

  private def colorizeShape(s:Shape, fColor:Color, strColor:Color):Unit = {
    s.setFill(fColor)
    s.setStroke(strColor)
  }

  def drawRectangle(point:Point, width:Double, height:Double)(fillColor:Color, strokeColor:Color):Unit = {
    val (x,y) = point
    val rectangle = new Rectangle(x,y,width,height)
    colorizeShape(rectangle, fillColor, strokeColor)
    drawShape(rectangle)
  }

  def drawLine(start:Point, end:Point)(fillColor:Color, strokeColor:Color):Unit = {
    val (startX, startY) = start
    val (endX, endY) = end
    val line = new Line(startX,startY, endX,endY)
    colorizeShape(line, fillColor, strokeColor)
    drawShape(line)
  }

  def drawCircle(point:Point, width:Double, height:Double)(fillColor:Color, strokeColor:Color):Unit = {
    val (x,y) = point
    val circle = new Ellipse(x,y, width, height)
    colorizeShape(circle, fillColor, strokeColor)
    drawShape(circle)
  }

  def drawPolygon(points:List[Point])(fillColor:Color, strokeColor:Color):Unit = {
    val singlePoints = points.flatMap { case (x,y) => List(x,y) }
    val polygon = new Polygon(singlePoints:_*)
    colorizeShape(polygon, fillColor, strokeColor)
    drawShape(polygon)
  }

}
