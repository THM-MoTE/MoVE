/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views.shapes

import javafx.scene.paint.Paint
import javafx.scene.paint.Color
import javafx.scene.shape.Shape
import de.thm.move.models.LinePattern
import de.thm.move.models.FillPattern
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.ObjectProperty

trait ColorizableShape {
  self: Shape =>

  val linePattern:ObjectProperty[LinePattern.Value] =
    new SimpleObjectProperty(LinePattern.Solid)

  val fillPatternProperty:ObjectProperty[FillPattern.Value] =
    new SimpleObjectProperty(FillPattern.Solid)
  val oldFillColorProperty:ObjectProperty[Color] =
    new SimpleObjectProperty(null) //null = transparent

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
    c match {
      case color:Color => oldFillColorProperty.set(color)
      case _ => //ignore
    }
  }
  def setStrokeColor(c:Paint): Unit = {
    self.setStroke(c)
  }
}
