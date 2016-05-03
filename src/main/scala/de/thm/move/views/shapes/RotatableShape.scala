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

  private val topLeftAnchor = new Anchor(0,0) with RotateAnchor
  private val topRightAnchor = new Anchor(0,0) with RotateAnchor
  private val bottomLeftAnchor = new Anchor(0,0) with RotateAnchor
  private val bottomRightAnchor = new Anchor(0,0) with RotateAnchor

  val rotationAnchors = List(topLeftAnchor, topRightAnchor, bottomLeftAnchor,bottomRightAnchor)
  rotationAnchors.foreach(setupListener)
  boundsInLocalProperty().addListener { (_:Bounds, newB:Bounds) =>
    topLeftAnchor.setCenterX(newB.getMinX)
    topLeftAnchor.setCenterY(newB.getMinY)

    topRightAnchor.setCenterX(newB.getMaxX)
    topRightAnchor.setCenterY(newB.getMinY)

    bottomLeftAnchor.setCenterX(newB.getMinX)
    bottomLeftAnchor.setCenterY(newB.getMaxY)

    bottomRightAnchor.setCenterX(newB.getMaxX)
    bottomRightAnchor.setCenterY(newB.getMaxY)
  }

  private def setupListener(anchor:Anchor): Unit = {
    var startMouse = (0.0,0.0)
    //undo-/redo command
    var command: (=> Unit) => Command = x => { History.emptyAction }

    anchor.setOnMousePressed(withConsumedEvent { me:MouseEvent =>
      startMouse = (me.getSceneX,me.getSceneY)
      val oldDegree = getRotate
      command = History.partialAction {
        setRotate(oldDegree)
      }
    })
    anchor.setOnMouseDragged(withConsumedEvent { me: MouseEvent =>
      val newP = (me.getSceneX,me.getSceneY)
      val delta = startMouse - newP
      startMouse = newP
      val rotationAndY = getRotate + delta.y*(-1)
      val rotateDegree =
        if(rotationAndY < 360) rotationAndY
        else 0

      setRotate(rotateDegree)
    })
    anchor.setOnMouseReleased(withConsumedEvent { _: MouseEvent =>
      val newDegree = getRotate
      history.save(command {
        setRotate(newDegree)
      })
    })
  }
}
