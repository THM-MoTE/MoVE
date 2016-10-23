/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.views.shapes

import de.thm.move.Global._
import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.history.History
import de.thm.move.history.History.Command
import de.thm.move.util.JFxUtils._
import de.thm.move.util.PointUtils._
import de.thm.move.util.GeometryUtils
import de.thm.move.views.anchors.{Anchor, RotateAnchor}
import javafx.geometry.Bounds
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.shape.Circle
import javafx.scene.paint.Color
import javafx.scene.layout.Pane

/** Turns a Node into a rotatable node by adding anchors for rotation and rotate the element accordingly. */
trait RotatableShape {
  this:Node =>

  private val topLeftAnchor = new Anchor(0,0) with RotateAnchor
  private val topRightAnchor = new Anchor(0,0) with RotateAnchor
  private val bottomLeftAnchor = new Anchor(0,0) with RotateAnchor
  private val bottomRightAnchor = new Anchor(0,0) with RotateAnchor

  private def usedBounds = getBoundsInLocal()
  private def topLeft = (usedBounds.getMinX, usedBounds.getMinY)
  private def topRight = (usedBounds.getMaxX, usedBounds.getMinY)
  private def bottomLeft = (usedBounds.getMinX, usedBounds.getMaxY)
  private def bottomRight = (usedBounds.getMaxX, usedBounds.getMaxY)

  val rotationAnchors = List(topLeftAnchor, topRightAnchor, bottomLeftAnchor,bottomRightAnchor)
  rotationAnchors.foreach(setupListener)
  boundsInParentProperty().addListener { (_:Bounds, newB:Bounds) =>
      //align rotation-anchors to the local bounding-box
    topLeftAnchor.setCenterX(newB.getMinX)
    topLeftAnchor.setCenterY(newB.getMinY)

    topRightAnchor.setCenterX(newB.getMaxX)
    topRightAnchor.setCenterY(newB.getMinY)

    bottomLeftAnchor.setCenterX(newB.getMinX)
    bottomLeftAnchor.setCenterY(newB.getMaxY)

    bottomRightAnchor.setCenterX(newB.getMaxX)
    bottomRightAnchor.setCenterY(newB.getMaxY)
  }

  def rotate(degree:Double) = setRotate(getRotate+degree)

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
      //translated from:
      //http://www.mathe-online.at/materialien/Andreas.Pester/files/Vectors/winkel_zwischen_vektoren.htm
      val translatedStart = sceneToLocal(startMouse.x, startMouse.y)
      val translatedEnd = sceneToLocal(me.getSceneX, me.getSceneY)
      val middlePoint =
        GeometryUtils.rectangleMiddlePoint(
          topLeft, topRight, bottomLeft, bottomRight)

      val vector1 = GeometryUtils.vectorOf(middlePoint, (translatedStart.getX, translatedStart.getY))
      val vector2 = GeometryUtils.vectorOf(middlePoint, (translatedEnd.getX, translatedEnd.getY))
      val scalar = GeometryUtils.scalarProduct(vector1,vector2) //scalarproduct
      val lengthVector1 = GeometryUtils.vectorLength(vector1) //norm/length vector1
      val lengthVector2 = GeometryUtils.vectorLength(vector2) //norm/length vector2
      val cos_angle = scalar / (lengthVector1 * lengthVector2)
      val angle =
        if(translatedEnd.getY > translatedStart.getY && (anchor == topRightAnchor || anchor == bottomRightAnchor))
          scala.math.acos(cos_angle)
        else if(translatedEnd.getY > translatedStart.getY && (anchor == topLeftAnchor || anchor == bottomLeftAnchor))
          scala.math.acos(cos_angle)*(-1)
        else if(anchor == topLeftAnchor || anchor == bottomLeftAnchor)
          scala.math.acos(cos_angle)
        else
          scala.math.acos(cos_angle)*(-1)

      setRotate(getRotate + angle)
    })
    anchor.setOnMouseReleased(withConsumedEvent { _: MouseEvent =>
      val newDegree = getRotate
      history.save(command {
        setRotate(newDegree)
      })
    })
  }
}
