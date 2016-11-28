package de.thm.move.controllers.drawing

import javafx.beans.property.{BooleanProperty, SimpleBooleanProperty}
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Paint

import de.thm.move.controllers.ChangeDrawPanelLike
import de.thm.move.types._
import de.thm.move.util.GeometryUtils
import de.thm.move.views.shapes.{ResizableCircle, ResizableRectangle}

class CircleStrategy(changeLike:ChangeDrawPanelLike) extends DrawStrategy {
  private var tmpFigure = new ResizableCircle((0,0), 0,0)
  private var startPoint:Point = (0,0)
  tmpFigure.setId(tmpShapeId)

  protected def setBounds(point:Point): Unit = {
    val (width,height) = point - startPoint
    val (middleX, middleY) = GeometryUtils.middleOfLine(startPoint, point)
    tmpFigure.setX(middleX)
    tmpFigure.setY(middleY)
    if(drawConstraintProperty.get) {
      val tmpDelta = width min height
      tmpFigure.setWidth(tmpDelta)
      tmpFigure.setHeight(tmpDelta)
    } else {
      tmpFigure.setWidth(width)
      tmpFigure.setHeight(height)
    }
  }

  def reset(): Unit = {
    tmpFigure.setXY((0,0))
    tmpFigure.setWidth(0)
    tmpFigure.setHeight(0)
    changeLike.remove(tmpFigure)
    startPoint = (0,0)
  }

  override lazy val drawConstraintProperty = new SimpleBooleanProperty(false)

  override def dispatchEvent(mouseEvent: MouseEvent): Unit = mouseEvent.getEventType match {
    case MouseEvent.MOUSE_PRESSED =>
      changeLike.addNode(tmpFigure)
      startPoint = (mouseEvent.getX, mouseEvent.getY)
      tmpFigure.setXY(startPoint)
    case MouseEvent.MOUSE_DRAGGED =>
      setBounds(mouseEvent.getX, mouseEvent.getY)
    case MouseEvent.MOUSE_RELEASED =>
      setBounds(mouseEvent.getX, mouseEvent.getY)
      changeLike.addShapeWithAnchors(tmpFigure.copy)
      reset()
    case _ => //ignore
  }

  override def setColor(fill:Paint, stroke:Paint, strokeThickness:Int):Unit = {
    tmpFigure.setFillColor(fill)
    tmpFigure.setStrokeColor(stroke)
    tmpFigure.setStrokeWidth(strokeThickness)
  }
}
