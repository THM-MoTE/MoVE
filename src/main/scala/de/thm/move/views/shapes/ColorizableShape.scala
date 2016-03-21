package de.thm.move.views.shapes

import javafx.scene.paint.Color
import javafx.scene.shape.Shape

trait ColorizableShape {
  self: Shape =>

  def colorizeShape(fillColor:Color, strokeColor:Color): Unit = {
    self.setFill(fillColor)
    self.setStroke(strokeColor)
  }

  def setStrokeWidth(width:Int): Unit = self.strokeWidthProperty().set(width)
  def setFillColor(c:Color): Unit = self.setFill(c)
  def setStrokeColor(c:Color): Unit = self.setStroke(c)
}
