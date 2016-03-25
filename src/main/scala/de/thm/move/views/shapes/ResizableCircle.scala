/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.views.shapes

import javafx.scene.shape.Ellipse
import javafx.geometry.Bounds

import de.thm.move.models.CommonTypes._
import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.util.BindingUtils
import de.thm.move.views.Anchor
import javafx.scene.input.MouseEvent

class ResizableCircle(
        point:Point,
        width:Double,
        height:Double) extends Ellipse(point._1, point._2, width, height) with ResizableShape with BoundedAnchors with ColorizableShape {
  private val (x,y) = point

  override val adjustCoordinates = false

  override def setX(x: Double): Unit = setCenterX(x)

  override def setY(y: Double): Unit = setCenterY(y)

  override def setWidth(w: Double): Unit = setRadiusX(w/2)

  override def setHeight(h: Double): Unit = setRadiusY(h/2)

  override def getX: Double = getCenterX

  override def getY: Double = getCenterY

  override def getWidth: Double = getBoundsInLocal.getWidth
  override def getHeight: Double = getBoundsInLocal.getHeight

}
