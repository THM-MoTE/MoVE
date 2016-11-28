package de.thm.move.controllers.drawing

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Paint

import de.thm.move.controllers.ChangeDrawPanelLike
import de.thm.move.views.shapes._
import de.thm.move.types._

class RectangleStrategy(changeLike:ChangeDrawPanelLike) extends DrawStrategy {
  private var tmpFigure = new ResizableRectangle((0,0), 0,0)
  tmpFigure.setId(tmpShapeId)

  override lazy val drawConstraintProperty = new SimpleBooleanProperty(false)

  protected def setBounds(point:Point): Unit = {
    val (width,height) = point - tmpFigure.getXY
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
  }

  override def dispatchEvent(mouseEvent: MouseEvent): Unit = {
    mouseEvent.getEventType match {
      case MouseEvent.MOUSE_PRESSED =>
        changeLike.addNode(tmpFigure)
        tmpFigure.setXY(mouseEvent.getX, mouseEvent.getY)
      case MouseEvent.MOUSE_DRAGGED =>
        setBounds(mouseEvent.getX, mouseEvent.getY)
      case MouseEvent.MOUSE_RELEASED =>
        setBounds(mouseEvent.getX, mouseEvent.getY)
        changeLike.addShapeWithAnchors(tmpFigure.copy)
        reset()
      case _ => //ignore
    }
  }

  override def setColor(fill:Paint, stroke:Paint, strokeThickness:Int):Unit = {
    tmpFigure.setFillColor(fill)
    tmpFigure.setStrokeColor(stroke)
    tmpFigure.setStrokeWidth(strokeThickness)
  }
}
