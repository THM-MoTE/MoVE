package de.thm.move.controllers.drawing

import javafx.scene.input.MouseEvent
import javafx.scene.paint.Paint

import de.thm.move.controllers.ChangeDrawPanelLike
import de.thm.move.types._

abstract class RectangularStrategy(changeLike:ChangeDrawPanelLike, protected val tmpFigure:RectangularNode) extends DrawStrategy {
  override type FigureType = RectangularNode
  tmpFigure.setId(tmpShapeId)

  override def reset(): Unit = {
    tmpFigure.setXY((0,0))
    tmpFigure.setWidth(0)
    tmpFigure.setHeight(0)
    changeLike.remove(tmpFigure)
  }

  protected def setStartXY(p:Point): Unit = {
    tmpFigure.setXY(p)
  }

  protected def calculateBounds(point: Point):Point = {
    val (width,height) = point - tmpFigure.getXY
    if(drawConstraintProperty.get) {
      val tmpDelta = width min height
      (tmpDelta, tmpDelta)
    } else {
      (width, height)
    }
  }

  protected def setBounds(point:Point): Unit = {
    val (width, height) = calculateBounds(point)
    tmpFigure.setWidth(width)
    tmpFigure.setHeight(height)
  }

  override def dispatchEvent(mouseEvent: MouseEvent): Unit = {
    mouseEvent.getEventType match {
      case MouseEvent.MOUSE_PRESSED =>
        changeLike.addNode(tmpFigure)
        setStartXY(mouseEvent.getX, mouseEvent.getY)
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
