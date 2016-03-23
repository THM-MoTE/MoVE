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
import de.thm.move.views.Anchor
import de.thm.move.controllers.implicits.FxHandlerImplicits._

import de.thm.move.Global

class ResizableRectangle(
            startPoint:Point,
            width:Double,
            height:Double) extends Rectangle(startPoint._1, startPoint._2, width, height) with ResizableShape with BoundedAnchors with ColorizableShape {
  private val (x,y) = startPoint
/*
  topLeftAnchor.setOnMouseDragged({me:MouseEvent =>
    val (newX, newY) = (me.getX, me.getY)
    val (oldX, oldY) = (this.getX, this.getY)
    val deltaY = if(newY < oldY) getHeight + (oldY-newY)else getHeight - (newY-oldY)
    val deltaX = if(newX > oldX) getWidth + (oldX-newX) else getWidth - (newX-oldX)
    this.setY(newY)
    this.setHeight(deltaY)
    this.setX(newX)
    this.setWidth(deltaX)
  })

  topRightAnchor.setOnMouseDragged({ me: MouseEvent =>
    val (newX, newY) = (me.getX, me.getY)
    val (oldX, oldY) = (this.getX+this.getWidth, this.getY)
    val deltaY = if(newY < oldY) getHeight + (oldY-newY) else getHeight - (newY-oldY)
    val deltaX = if(newX > oldX) getWidth + (newX-oldX) else getWidth - (oldX-newX)

    this.setY(newY)
    this.setHeight(deltaY)
    this.setWidth(deltaX)
  })

  bottomLeftAnchor.setOnMouseDragged({ me: MouseEvent =>
    val (newX, newY) = (me.getX, me.getY)
    val (oldX, oldY) = (this.getX, this.getY+getHeight)

    val deltaY = (newY-oldY) + getHeight
    val deltaX = (oldX-newX) + getWidth

    this.setX(newX)
    this.setHeight(deltaY)
    this.setWidth(deltaX)
  })

  bottomRightAnchor.setOnMouseDragged({ me: MouseEvent =>
    val (newX, newY) = (me.getX, me.getY)
    val (oldX, oldY) = (this.getX+this.getWidth, this.getY+this.getHeight)
    val deltaX = (newX-oldX) + getWidth
    val deltaY = (newY-oldY) + getHeight
    setWidth(deltaX)
    setHeight(deltaY)
  })*/

  override def getTopLeft:Point = (getX, getY)
  override def getTopRight:Point = (getX + getWidth, getY)
  override def getBottomLeft:Point = (getX, getY + getHeight)
  override def getBottomRight:Point = (getX + getWidth,getY + getHeight)
}
