/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.views.shapes

import javafx.scene.Node
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.input.KeyEvent
import javafx.scene.input.KeyCode
import de.thm.move.views.Anchor
import de.thm.move.Global
import de.thm.move.controllers.implicits.FxHandlerImplicits._

trait ResizableShape extends Node {

  val resizeProportionalProperty = new SimpleBooleanProperty(false)
  val selectionRectangle = new SelectionRectangle(this)

  def getAnchors: List[Anchor]

  def getX: Double
  def getY: Double

  def setX(x:Double): Unit
  def setY(y:Double): Unit
}
