package de.thm.move.controllers

import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.shape.Shape

import de.thm.move.models.CommonTypes._
import de.thm.move.models.SelectedShape
import de.thm.move.models.SelectedShape._
import de.thm.move.views.{Anchor, DrawPanel}

class DrawCtrl(drawPanel: DrawPanel) {

  def getDrawHandler: (SelectedShape, MouseEvent) => (Color, Color, Int) => Unit = {
    var points = List[Point]()

    def drawHandler(shape:SelectedShape, mouseEvent:MouseEvent)(fillColor:Color, strokeColor:Color, selectedThickness:Int): Unit = {
      shape match {
        case SelectedShape.Polygon =>
          if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
            val newX = mouseEvent.getX()
            val newY = mouseEvent.getY()
            //test if polygon is finish by checking if last clicked position is already in the coordinate list
            points.find {
              case (x, y) => Math.abs(x - newX) <= 10 && Math.abs(y - newY) <= 10
            } match {
              case Some(_) =>
                //draw the polygon
                drawPanel.drawPolygon(points)(fillColor, strokeColor)
                points = List()
              case None =>
                points = (newX, newY) :: points
                drawPanel.drawAnchor(points.head)
            }
          }
        case _ =>
          if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
            points = (mouseEvent.getX(), mouseEvent.getY()) :: points
          } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
            points = (mouseEvent.getX(), mouseEvent.getY()) :: points

            points match {
              case end :: start :: _ => drawCustomShape(shape, start, end)(fillColor, strokeColor, selectedThickness)
            }
            points = List()
          }
      }
    }

    drawHandler
  }

  def getMoveHandler: (MouseEvent => Unit) = {
    var deltaX = -1.0
    var deltaY = -1.0
    def moveElement(mv:MouseEvent): Unit = {
      //move selected element
      mv.getEventType match {
        case MouseEvent.MOUSE_PRESSED =>
          //save original coordinate
          println("old: "+mv.getSource)
          println(mv.getSource.asInstanceOf[Shape].getLayoutX)
          println(mv.getSource.asInstanceOf[Shape].getLayoutY)
          mv.getSource match {
            case a:Anchor =>
              deltaX = a.getCenterX - mv.getSceneX
              deltaY = a.getCenterY - mv.getSceneY
            case s:Shape =>
              deltaX = s.getLayoutX - mv.getSceneX
              deltaY = s.getLayoutY - mv.getSceneY
            case _ => throw new IllegalStateException("shapeInputHandler: source isn't a shape")
          }
        case MouseEvent.MOUSE_DRAGGED =>
          //translate from original to new position
          mv.getSource match {
            case a:Anchor =>
              a.setCenterX(deltaX + mv.getSceneX)
              a.setCenterY(deltaY + mv.getSceneY)
            case s:Shape =>
              s.setLayoutX(deltaX + mv.getSceneX)
              s.setLayoutY(deltaY + mv.getSceneY)
            case _ => throw new IllegalStateException("shapeInputHandler: source isn't a shape")
          }
        case MouseEvent.MOUSE_RELEASED =>
          println("new: " + mv.getSource)
          println(mv.getSource.asInstanceOf[Shape].getLayoutX)
          println(mv.getSource.asInstanceOf[Shape].getLayoutY)
        case _ => //unknown event
      }
    }
    moveElement
  }

  def drawCustomShape(shape:SelectedShape, start:Point, end:Point)(fillColor:Color, strokeColor:Color, selectedThickness:Int) = {
    val (startX, startY) = start
    val (endX, endY) = end

    shape match {
      case SelectedShape.Rectangle =>
        val width = endX - startX
        val height = endY - startY
        drawPanel.drawRectangle(start, width, height)(fillColor, strokeColor)
      case SelectedShape.Circle =>
        val width = endX - startX
        val height = endY - startY
        drawPanel.drawCircle(start, width, height)(fillColor, strokeColor)
      case SelectedShape.Line => drawPanel.drawLine(start, end, selectedThickness)(fillColor, strokeColor)
      case _ => //ignore
    }
  }
}
