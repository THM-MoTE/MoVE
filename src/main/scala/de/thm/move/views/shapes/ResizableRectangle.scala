/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.views.shapes

import javafx.geometry.Bounds
import javafx.scene.input.MouseEvent
import javafx.scene.shape.Rectangle
import javafx.scene.input.KeyEvent
import javafx.event.EventHandler

import de.thm.move.models.CommonTypes.Point
import de.thm.move.views.{ShapeContextMenu, Anchor}
import de.thm.move.controllers.implicits.FxHandlerImplicits._

import de.thm.move.Global

class ResizableRectangle(
            startPoint:Point,
            width:Double,
            height:Double) extends Rectangle(startPoint._1, startPoint._2, width, height) with ResizableShape with BoundedAnchors with ColorizableShape {
  private val (x,y) = startPoint

  override def getTopLeft:Point = (getX, getY)
  override def getTopRight:Point = (getX + getWidth, getY)
  override def getBottomLeft:Point = (getX, getY + getHeight)
  override def getBottomRight:Point = (getX + getWidth,getY + getHeight)
  override def copy: ResizableShape = {
    val duplicate = new ResizableRectangle(startPoint, width, height)
    duplicate.copyColors(this)
    duplicate.copyPosition(this)
    duplicate
  }
}
