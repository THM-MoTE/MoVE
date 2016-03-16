package de.thm.move.views

import javafx.event.{EventHandler, EventType}
import javafx.scene.Cursor
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape._
import javafx.scene.input.{InputEvent, MouseDragEvent, MouseEvent}
import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.models.CommonTypes._

class DrawPanel(inputEventHandler : InputEvent => Unit) extends Pane {
  private var shapes = List[Shape]()

  this.setMaxWidth(Double.MaxValue)
  this.setMaxHeight(Double.MaxValue)

  def drawShape(s:Shape):Unit = {
    s.addEventHandler(InputEvent.ANY, inputEventHandler)

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

  def drawLine(start:Point, end:Point, strokeSize:Int)(fillColor:Color, strokeColor:Color):Unit = {
    val (startX, startY) = start
    val (endX, endY) = end
    val line = new Line(startX,startY, endX,endY)
    colorizeShape(line, fillColor, strokeColor)
    line.setStrokeWidth(strokeSize.toDouble)
    drawShape(line)
  }

  def drawCircle(point:Point, width:Double, height:Double)(fillColor:Color, strokeColor:Color):Unit = {
    val (x,y) = point
    val circle = new Ellipse(x,y, width, height)
    colorizeShape(circle, fillColor, strokeColor)
    drawShape(circle)
  }

  def drawAnchor(point:Point)(fillColor:Color):Unit = {
    val (x,y) = point
    drawShape(new Anchor(x,y,fillColor))
  }

  def drawPolygon(points:List[Point])(fillColor:Color, strokeColor:Color):Unit = {
    val singlePoints = points.flatMap { case (x,y) => List(x,y) }
    val polygon = new Polygon(singlePoints:_*)
    colorizeShape(polygon, fillColor, strokeColor)
    removeDrawnAchors()
    drawShape(polygon)
  }

  private def removeDrawnAchors():Unit = {
    println(shapes)
    //get indexes of drawn anchors
    val removingAnchors = shapes.takeWhile( _.isInstanceOf[Anchor] )
    //remove from shapelist
    shapes = shapes.dropWhile(_.isInstanceOf[Anchor])

    //remove from painting area
    this.getChildren.removeAll(removingAnchors:_*)
  }

}
