package de.thm.move.controllers.drawing

import javafx.scene.paint.Paint

import de.thm.move.controllers.ChangeDrawPanelLike
import de.thm.move.types.{PathNode, Point}

import scala.collection.mutable

abstract class PathLikeStrategy(changeLike:ChangeDrawPanelLike) extends DrawStrategy {
    //tmpFigure is anything that is a PathNode
  type FigureType <: PathNode
  protected val pointBuffer = mutable.ListBuffer[Point]()

  def setColor(fill:Paint, stroke:Paint, strokeThickness:Int): Unit = {
    tmpFigure.setFillColor(fill)
    tmpFigure.setStrokeColor(stroke)
    tmpFigure.setStrokeWidth(strokeThickness)
  }
}
