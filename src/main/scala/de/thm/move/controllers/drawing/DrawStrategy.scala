package de.thm.move.controllers.drawing

import javafx.beans.property.BooleanProperty
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Paint

import de.thm.move.types._
import de.thm.move.views.panes.DrawPanel

trait DrawStrategy {

  val tmpShapeId = DrawPanel.tmpShapeId + "drawctrl"

  def drawConstraintProperty: BooleanProperty
//  def drawStart(point:Point): Unit
//  def drawIntermediate(point:Point): Unit
//  def drawEnd(point:Point): Unit
//  def reset(): Unit
  def dispatchEvent(mouseEvent:MouseEvent): Unit
  def setColor(fill:Paint, stroke:Paint, strokeThickness:Int): Unit
}
