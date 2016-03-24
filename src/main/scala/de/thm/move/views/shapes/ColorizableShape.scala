/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.views.shapes

import javafx.scene.paint.Color
import javafx.scene.shape.Shape

import de.thm.move.Global
import de.thm.move.history.History
import de.thm.move.history.History.Command

trait ColorizableShape {
  self: Shape =>

  def colorizeShape(fillColor:Color, strokeColor:Color): Unit = {
    self.setFillColor(fillColor)
    self.setStrokeColor(strokeColor)
  }

  def setStrokeWidth(width:Int): Unit = {
    val oldWidth = self.strokeWidthProperty().get()
    val cmd = History.newCommand(self.strokeWidthProperty().set(width), self.strokeWidthProperty().set(oldWidth))
    Global.history.execute(cmd)
  }
  def setFillColor(c:Color): Unit = {
    val oldColor = self.getFill
    val cmd =  History.newCommand(self.setFill(c), self.setFill(oldColor))
    Global.history.execute(cmd)
  }
  def setStrokeColor(c:Color): Unit = {
    val oldColor = self.getFill
    val cmd =  History.newCommand(self.setStroke(c), self.setStroke(oldColor))
    Global.history.execute(cmd)
  }
}
