/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.views.shapes

import javafx.scene.paint.{Paint, Color}
import javafx.scene.shape.Shape

import de.thm.move.Global
import de.thm.move.history.History
import de.thm.move.history.History.Command

trait ColorizableShape {
  self: Shape =>

  def colorizeShape(fillColor:Paint, strokeColor:Paint): Unit = {
    self.setFillColor(fillColor)
    self.setStrokeColor(strokeColor)
  }

  def getStrokeWidth: Double
  def getFillColor:Paint = self.getFill
  def getStrokeColor:Paint = self.getStroke

  def setStrokeWidth(width:Double): Unit
  def setFillColor(c:Paint): Unit = {
    self.setFill(c)
  }
  def setStrokeColor(c:Paint): Unit = {
    self.setStroke(c)
  }
}
