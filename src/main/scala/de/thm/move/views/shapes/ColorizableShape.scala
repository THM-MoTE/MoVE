/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.views.shapes

import javafx.beans.property.{ObjectProperty, SimpleObjectProperty}
import javafx.scene.paint.{Color, Paint}
import javafx.scene.shape.Shape

import de.thm.move.models.pattern._
import de.thm.move.util.JFxUtils._
import de.thm.move.implicits.FxHandlerImplicits._

/** A colorizable shape. */
trait ColorizableShape {
  self: Shape =>

  /** Pattern of the stroke */
  val linePattern:ObjectProperty[LinePattern] =
    new SimpleObjectProperty(SSolid)


  linePattern.addListener { (_:LinePattern, newlp:LinePattern) =>
    newlp.applyToShape(this)
  }

  /** Pattern of the fill */
  val fillPatternProperty:ObjectProperty[FillPattern] =
    new SimpleObjectProperty(FSolid)

  fillPatternProperty.addListener { (_:FillPattern, newfp:FillPattern) =>
    newfp.applyToShape(this)
  }

  /** The old/actual Color fill.
    *
    * @note
    *       If you add gradients, images, etc. as fill you lost the actual color behind the fill,
    *       this property saves the color for later usgae.
    */
  val oldFillColorProperty:ObjectProperty[Color] =
    new SimpleObjectProperty(null) //null = transparent

  /** Copies the style from other to this element */
  def copyColors(other:ColorizableShape): Unit = {
    setFillColor(other.getFillColor)
    setStrokeColor(other.getStrokeColor)
    setStrokeWidth(other.getStrokeWidth)
    copyProperty(fillPatternProperty, other.fillPatternProperty)
    copyProperty(oldFillColorProperty, other.oldFillColorProperty)
    copyProperty(linePattern, other.linePattern)
  }

  /** Sets the fill and stroke color of this shape */
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
