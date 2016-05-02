/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views.shapes

import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Bounds
import javafx.scene.input.MouseEvent

import de.thm.move.Global._
import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.history.History
import de.thm.move.history.History.Command
import de.thm.move.util.JFxUtils._
import de.thm.move.util.GeometryUtils
import de.thm.move.util.PointUtils._
import de.thm.move.views.Anchor
import de.thm.move.views.RotateAnchor
import javafx.scene.Node

/** Turns a Node into a rotatable node by adding a anchor for rotation and rotate the element accordingly. */
trait RotatableShape {
  this:Node =>

  //show the anchor always at the top of the element
  private val xProp = new SimpleDoubleProperty(getBoundsInLocal.getMinX)
  private val yProp = new SimpleDoubleProperty(getBoundsInLocal.getMinY)
  boundsInLocalProperty().addListener { (_:Bounds, newB:Bounds) =>
    val (newX,newY) = GeometryUtils.middleOfLine(newB.getMinX, newB.getMinY, newB.getMaxX, newB.getMinY)
    xProp.set(newX)
    yProp.set(newY)
  }

  val rotationAnchor = new Anchor(0,0) with RotateAnchor
  rotationAnchor.centerXProperty().bind(xProp)
  rotationAnchor.centerYProperty().bind(yProp)


  private var startMouse = (0.0,0.0)
  //undo-/redo command
  private var command: (=> Unit) => Command = x => { History.emptyAction }

  rotationAnchor.setOnMousePressed(withConsumedEvent { me:MouseEvent =>
    startMouse = (me.getSceneX,me.getSceneY)
    val oldDegree = getRotate
    command = History.partialAction {
      setRotate(oldDegree)
    }
  })

  rotationAnchor.setOnMouseDragged(withConsumedEvent { me: MouseEvent =>
    val newP = (me.getSceneX,me.getSceneY)
    val delta = startMouse - newP
    startMouse = newP
    val rotationAndY = getRotate + delta.y*(-1)
    val rotateDegree =
      if(rotationAndY < 360) rotationAndY
      else 0

    setRotate(rotateDegree)
  })

  rotationAnchor.setOnMouseReleased(withConsumedEvent { _: MouseEvent =>
    val newDegree = getRotate
    history.save(command {
      setRotate(newDegree)
    })
  })
}
