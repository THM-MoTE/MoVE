/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.views.shapes

import javafx.scene.paint.Paint
import javafx.scene.shape.Shape

trait ColorizableShape {
  self: Shape =>

  /** Copies the style from other to this element */
  def copyColors(other:ColorizableShape): Unit = {
    setFillColor(other.getFillColor)
    setStrokeColor(other.getStrokeColor)
    setStrokeWidth(other.getStrokeWidth)
  }

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
