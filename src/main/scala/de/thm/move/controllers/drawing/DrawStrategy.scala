package de.thm.move.controllers.drawing

import javafx.beans.property.{BooleanProperty, SimpleBooleanProperty}
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Paint

import de.thm.move.util.Resettable
import de.thm.move.views.panes.DrawPanel

trait DrawStrategy extends Resettable {

  val tmpShapeId = DrawPanel.tmpShapeId

  type FigureType
  protected def tmpFigure:FigureType

  lazy val drawConstraintProperty: BooleanProperty = new SimpleBooleanProperty(false)
  def dispatchEvent(mouseEvent:MouseEvent): Unit
  def setColor(fill:Paint, stroke:Paint, strokeThickness:Int): Unit
}
