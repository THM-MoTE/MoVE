package de.thm.move.controllers.drawing

import javafx.beans.property.{BooleanProperty, SimpleBooleanProperty}
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Paint

import de.thm.move.types._
import de.thm.move.util.Resettable
import de.thm.move.views.panes.DrawPanel

trait DrawStrategy extends Resettable {

  val tmpShapeId = DrawPanel.tmpShapeId + "drawctrl"

  lazy val drawConstraintProperty: BooleanProperty = new SimpleBooleanProperty(false)
//  def drawStart(point:Point): Unit
//  def drawIntermediate(point:Point): Unit
//  def drawEnd(point:Point): Unit
//  def reset(): Unit
  def dispatchEvent(mouseEvent:MouseEvent): Unit
  def setColor(fill:Paint, stroke:Paint, strokeThickness:Int): Unit
}
