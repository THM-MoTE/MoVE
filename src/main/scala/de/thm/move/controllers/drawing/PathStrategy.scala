package de.thm.move.controllers.drawing

import javafx.scene.input.MouseEvent

import de.thm.move.controllers.ChangeDrawPanelLike
import de.thm.move.types._
import de.thm.move.views.shapes.ResizablePath

class PathStrategy(changeLike:ChangeDrawPanelLike) extends PathLikeStrategy(changeLike) {
  override type FigureType = ResizablePath
  private val emptyPath = List((0.0,0.0))
  private var figure = ResizablePath(emptyPath)
  override protected def tmpFigure = figure
  figure.setId(tmpShapeId)

  protected def updatePath(points:List[Point]): Unit = {
    println(points)
    val oldShape = figure
    figure = ResizablePath(points)
    figure.copyColors(oldShape)
    figure.setId(tmpShapeId)
    changeLike.remove(figure)
    if(points != emptyPath)
      changeLike.addShapeWithAnchors(figure)
  }

  override def reset(): Unit = {
    updatePath(emptyPath)
    pointBuffer.clear()
  }

  def dispatchEvent(mouseEvent:MouseEvent): Unit = mouseEvent.getEventType match {
    case MouseEvent.MOUSE_CLICKED if mouseEvent.getClickCount == 2 =>
      changeLike.addShapeWithAnchors(figure.copy)
      reset()
    case MouseEvent.MOUSE_CLICKED =>
      pointBuffer += (mouseEvent.getX -> mouseEvent.getY)
      updatePath(pointBuffer.toList)
    case _ => //ignore
  }
}
