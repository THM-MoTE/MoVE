/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views.shapes

import javafx.scene.shape.Ellipse

import de.thm.move.models.CommonTypes._
import de.thm.move.util.GeometryUtils

/** A circle represented by it's '''middle-point''' and it's width + height */
class ResizableCircle(
        point:Point,
        width:Double,
        height:Double)
        extends Ellipse(point._1, point._2, width, height)
        with ResizableShape
        with RectangleLike
        with ColorizableShape {
  private val (x,y) = point

  override val adjustCoordinates = false

  override def setX(x: Double): Unit = setCenterX(x)

  override def setY(y: Double): Unit = setCenterY(y)

  override def setWidth(w: Double): Unit =
    setRadiusX(w/2)

  override def setHeight(h: Double): Unit =
    setRadiusY(h/2)

  override def getX: Double = getCenterX

  override def getY: Double = getCenterY

  override def getWidth: Double = getRadiusX*2
  override def getHeight: Double = getRadiusY*2
  override def copy: ResizableCircle = {
    val duplicate = new ResizableCircle(point, getRadiusX, getRadiusY)
    duplicate.copyPosition(this)
    duplicate.copyColors(this)
    duplicate
  }
}
