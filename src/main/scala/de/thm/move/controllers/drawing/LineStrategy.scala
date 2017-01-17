package de.thm.move.controllers.drawing

import javafx.scene.input.MouseEvent

import de.thm.move.controllers.ChangeDrawPanelLike
import de.thm.move.types._
import de.thm.move.views.shapes.ResizableLine

class LineStrategy(changeLike:ChangeDrawPanelLike) extends PathLikeStrategy(changeLike) {
  override type FigureType = ResizableLine
  override protected val tmpFigure = new ResizableLine((0,0), (0,0), 0)
  tmpFigure.setId(tmpShapeId)

  def setBounds(newX:Double, newY:Double): Unit = {
    val (deltaX, deltaY) = (newX -> newY) - pointBuffer.head
    if(drawConstraintProperty.get) {
      val (startX,startY) = pointBuffer.head
      val (x,y) = if(deltaX > deltaY) (newX,startY) else (startX,newY)
      tmpFigure.setEndX(x)
      tmpFigure.setEndY(y)
    } else {
      tmpFigure.setEndX(newX)
      tmpFigure.setEndY(newY)
    }
  }

  override def reset(): Unit = {
    pointBuffer.clear()
    changeLike.remove(tmpFigure)
    resetLine(0,0)
  }

  private def resetLine(x:Double, y:Double): Unit = {
    tmpFigure.setStartX(x)
    tmpFigure.setStartY(y)
    tmpFigure.setEndX(x)
    tmpFigure.setEndY(y)
  }

  def dispatchEvent(mouseEvent:MouseEvent): Unit = mouseEvent.getEventType match {
    case MouseEvent.MOUSE_PRESSED =>
      pointBuffer += (mouseEvent.getX -> mouseEvent.getY)
      resetLine(mouseEvent.getX, mouseEvent.getY)
      changeLike.addNode(tmpFigure)
    case MouseEvent.MOUSE_DRAGGED =>
      setBounds(mouseEvent.getX, mouseEvent.getY)
    case MouseEvent.MOUSE_RELEASED =>
      changeLike.addShapeWithAnchors(tmpFigure.copy)
      reset()
    case _ => //ignore
  }
}
