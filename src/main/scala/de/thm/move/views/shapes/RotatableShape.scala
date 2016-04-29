/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views.shapes

import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Bounds
import javafx.scene.input.MouseEvent

import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.util.GeometryUtils
import de.thm.move.util.PointUtils._
import de.thm.move.views.Anchor
import de.thm.move.views.RotateAnchor
import javafx.scene.Node

/** Adds rotation drag-points and rotates the element */
trait RotatableShape {
  this:Node =>

  val xProp = new SimpleDoubleProperty(getBoundsInLocal.getMinX)
  val yProp = new SimpleDoubleProperty(getBoundsInLocal.getMinY)
  boundsInLocalProperty().addListener { (_:Bounds, newB:Bounds) =>
    val (newX,newY) = GeometryUtils.middleOfLine(newB.getMinX, newB.getMinY, newB.getMaxX, newB.getMinY)
    xProp.set(newX)
    yProp.set(newY)
  }

  val rotationAnchor = new Anchor(0,0) with RotateAnchor
  rotationAnchor.centerXProperty().bind(xProp)
  rotationAnchor.centerYProperty().bind(yProp)


  var startMouse = (0.0,0.0)
  rotationAnchor.setOnMousePressed { me:MouseEvent =>
    startMouse = (me.getSceneX,me.getSceneY)
  }

  rotationAnchor.setOnMouseDragged { me: MouseEvent =>
    val newP = (me.getSceneX,me.getSceneY)
    val delta = startMouse - newP
    startMouse = newP
    val rotationAndY = getRotate + delta.y*(-1)
    val rotateDegree =
      if(rotationAndY < 360) rotationAndY
      else 0

    println(rotateDegree)
    setRotate(rotateDegree)
  }
}

object RotatableShape {
  /* Distance to the shape */
  val anchorDistance = -10.0
}
