package de.thm.move.views

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.value.{ObservableValue}
import javafx.event.{EventHandler}
import javafx.geometry.Bounds
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape._
import javafx.scene.image.{ImageView, Image}
import javafx.scene.input.{InputEvent}
import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.models.CommonTypes._
import javafx.beans.binding.Bindings

import de.thm.move.views.shapes._

class DrawPanel(inputEventHandler:InputEvent => Unit) extends Pane {
  private var shapes = List[Shape]()

  def drawImage(img:Image) = {
    val view = new ResizableImage(img)
    super.getChildren.add(view)
    super.getChildren.addAll(view.getAnchors:_*)
  }

  def drawShape(s:Shape):Unit = {
    s.addEventHandler(InputEvent.ANY, new EventHandler[InputEvent]() {
      override def handle(event: InputEvent): Unit = inputEventHandler(event)
    })

    super.getChildren.add(s)

    shapes = s :: shapes
  }

  def drawShapes(shapes:Shape*) = shapes.foreach(drawShape)

  private def colorizeShape(s:Shape, fColor:Color, strColor:Color):Unit = {
    s.setFill(fColor)
    s.setStroke(strColor)
  }

  def drawRectangle(point:Point, width:Double, height:Double)(fillColor:Color, strokeColor:Color):Unit = {
    val rectangle = new ResizableRectangle(point, width, height)
    rectangle.colorizeShape(fillColor, strokeColor)
    drawShapes(rectangle)
    drawShapes(rectangle.getAnchors:_*)
  }

  def drawLine(start:Point, end:Point, strokeSize:Int)(fillColor:Color, strokeColor:Color):Unit = {
    val line = new ResizableLine(start, end, strokeSize)
    line.colorizeShape(fillColor, strokeColor)
    drawShape(line)
    drawShapes(line.getAnchors:_*)
  }

  def drawCircle(point:Point, width:Double, height:Double)(fillColor:Color, strokeColor:Color):Unit = {
    val circle = new ResizableCircle(point, width, height)

    drawShape(circle)
    drawShapes(circle.getAnchors:_*)
  }

  def drawAnchor(point:Point):Unit = {
    val (x,y) = point
    drawShape(new Anchor(x,y))
  }

  def drawPolygon(points:List[Point])(fillColor:Color, strokeColor:Color):Unit = {
    val polygon = ResizablePolygon(points)
    polygon.colorizeShape(fillColor, strokeColor)
    removeDrawnAnchors(points.size+1)
    drawShape(polygon)
    drawShapes(polygon.getAnchors:_*)
  }

  private def removeDrawnAnchors(cnt:Int):Unit = {
    val removingAnchors = shapes.zipWithIndex.takeWhile {
      case (shape, idx) => shape.isInstanceOf[Anchor] && idx<cnt-1
    }.map(_._1)

    //remove from shapelist
    shapes = shapes.zipWithIndex.dropWhile {
      case (shape, idx) => shape.isInstanceOf[Anchor] && idx<cnt-1
    }.map(_._1)

    //remove from painting area
    this.getChildren.removeAll(removingAnchors:_*)
  }

  private def bindAnchorsTranslationToShapesLayout(shape:Shape)(anchors:Anchor*): Unit = {
    anchors.foreach { anchor =>
      anchor.layoutXProperty().bind(shape.layoutXProperty())
      anchor.layoutYProperty().bind(shape.layoutYProperty())
    }
  }
}
