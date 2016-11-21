/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.views.shapes

import javafx.geometry.{Bounds, Point2D}
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.{Circle, Ellipse}
import javafx.scene.transform.Affine

import de.thm.move.Global._
import de.thm.move.implicits.FxHandlerImplicits._
import de.thm.move.history.History
import de.thm.move.history.History.Command
import de.thm.move.models.CommonTypes._
import de.thm.move.util.GeometryUtils
import de.thm.move.util.JFxUtils._
import de.thm.move.util.PointUtils._
import de.thm.move.views.anchors.Anchor

/** Represents shapes with rectangular boundings.
  * For Example Rectangles, Circles, Images
  */
trait RectangleLike {
  self: ResizableShape =>

  private var startMouse = (0.0,0.0)

  /** Indicates if the coordinates of the shape should get adjusted too, when the
    * width and height get's adjusted.
    *
    * (On a circle the coordinates shouldn't get adjusted because the coordinate
    *  is in the middle of the circle!)
    */
  protected val adjustCoordinates:Boolean = true

  //resize anchors at edges
  val topLeftAnchor = new Anchor(getTopLeft)
  val topRightAnchor = new Anchor(getTopRight)
  val bottomLeftAnchor = new Anchor(getBottomLeft)
  val bottomRightAnchor = new Anchor(getBottomRight)
  override val getAnchors: List[Anchor] =
    List(topLeftAnchor, topRightAnchor, bottomLeftAnchor, bottomRightAnchor)

  /** Gets the untransformed upper-left point */
  def getTopLeft:Point = (getBoundsInLocal.getMinX, getBoundsInLocal.getMinY)
  /** Gets the untransformed upper-right point */
  def getTopRight:Point = (getBoundsInLocal.getMaxX, getBoundsInLocal.getMinY)
  /** Gets the untransformed bottom-left point */
  def getBottomLeft:Point = (getBoundsInLocal.getMinX, getBoundsInLocal.getMaxY)
  /** Gets the untransformed bottom-right point */
  def getBottomRight:Point = (getBoundsInLocal.getMaxX,getBoundsInLocal.getMaxY)

  /** Transforms the point src from local into parent's coordinate space */
  private def transformedPoint(src:Point):Point = {
    val (x,y) = src
    val point2D = localToParent(x,y)
    (point2D.getX, point2D.getY)
  }

  /** Gets the transformed upper-left point */
  def getTransformedTopLeft:Point = transformedPoint(getTopLeft)
  /** Gets the transformed upper-right point */
  def getTransformedTopRight:Point = transformedPoint(getTopRight)
  /** Gets the transformed bottom-left point */
  def getTransformedBottomLeft:Point = transformedPoint(getBottomLeft)
  /** Gets the transformed bottom-right point */
  def getTransformedBottomRight:Point = transformedPoint(getBottomRight)

  def adjustCenter(e:Ellipse, newPoint:Point): Unit = {
    val (x,y) = newPoint
    e.setCenterX(x)
    e.setCenterY(y)
  }

  /** Copies the position of '''other''' onto this element */
  def copyPosition( other:RectangleLike with ResizableShape ): Unit = {
      this.setXY(other.getXY)
      this.setWidth(other.getWidth)
      this.setHeight(other.getHeight)
      this.setRotate(other.getRotate)
  }

  def getX: Double
  def getY: Double
  final def getXY: Point = (getX, getY)

  def move(delta:Point):Unit = setXY(delta+getXY)
  def setX(x:Double): Unit
  def setY(y:Double): Unit
  final def setXY(p:Point): Unit = {
    setX(p._1)
    setY(p._2)
  }
  def getWidth: Double
  def getHeight: Double
  def setWidth(w:Double): Unit
  def setHeight(h:Double): Unit

  private val checkValue = 5.0

  /** Only sets the width if it's > a specific value */
  private def setCheckedWidth(w:Double): Unit = {
    if(w>checkValue)
      setWidth(w)
  }
  /** Only sets the height if it's > a specific value */
  private def setCheckedHeight(h:Double):Unit = {
    if(h>checkValue)
      setHeight(h)
  }

  private def withCheckedBounds(w:Double,h:Double)(fn: => Unit): Unit = {
    if(w>checkValue && h>checkValue) {
      setCheckedWidth(w)
      setCheckedHeight(h)
      fn
    }
  }

  def boundsChanged(): Unit = {
    adjustCenter(topLeftAnchor, getTransformedTopLeft)
    adjustCenter(topRightAnchor, getTransformedTopRight)
    adjustCenter(bottomLeftAnchor, getTransformedBottomLeft)
    adjustCenter(bottomRightAnchor, getTransformedBottomRight)
  }

  //adjust the anchors to the bounding-box
  boundsInLocalProperty().addListener { (_:Bounds, _:Bounds) =>
    boundsChanged()
  }
  //element got rotated; adjust anchors
  rotateProperty().addListener { (_:Number, _:Number) =>
    boundsChanged()
  }

  //undo-/redo command
  private var command: (=> Unit) => Command = x => { History.emptyAction }

  getAnchors.foreach { anchor =>
    anchor.setOnMouseReleased { _:MouseEvent =>
      val oldP = getXY
      val oldHeight = getHeight
      val oldWidth = getWidth

      history.save(command {
        if(adjustCoordinates) {
          setXY(oldP)
        }

        setWidth(oldWidth)
        setHeight(oldHeight)
      })
    }
  }

  topLeftAnchor.setOnMousePressed(withConsumedEvent { me: MouseEvent =>
    val (oldX, oldY) = getTopLeft
    val oldHeight = getHeight
    val oldWidth = getWidth

    startMouse = (me.getSceneX,me.getSceneY)

    command = History.partialAction {
      if(adjustCoordinates) {
        setX(oldX)
        setY(oldY)
      }
      setWidth(oldWidth)
      setHeight(oldHeight)
    }
  })

  topLeftAnchor.setOnMouseDragged(withConsumedEvent { me: MouseEvent =>
    val delta = calculateDelta(me)
    val isoCorner = getBottomRight.asJava
    val (oldX, oldY) = getTopLeft
    val boundWidth = getWidth
    val boundHeight = getHeight

    val oldMiddle = untransformedMiddlePoint.asJava
    withCheckedBounds(getWidth+delta.getX*(-1),getHeight+delta.getY*(-1)) {
      if(adjustCoordinates) {
        //use the new height & width for calculating the new x/y position
        setX(oldX - (getWidth-boundWidth))
        setY(oldY - (getHeight-boundHeight))
        val newMiddle = untransformedMiddlePoint.asJava
        val deltaP = GeometryUtils.calculateRotationOffset(oldMiddle, newMiddle, getRotate, isoCorner)
        setX(getX + deltaP.getX)
        setY(getY + deltaP.getY)
      }
    }
    startMouse = (me.getSceneX,me.getSceneY)
  })

  topRightAnchor.setOnMousePressed(withConsumedEvent { me: MouseEvent =>
    val (oldX, oldY) = getTopLeft
    val oldHeight = getHeight
    val oldWidth = getWidth

    startMouse = (me.getSceneX, me.getSceneY)

    command = History.partialAction {
      if(adjustCoordinates) {
        setY(oldY)
      }
      setWidth(oldWidth)
      setHeight(oldHeight)
    }
  })

  topRightAnchor.setOnMouseDragged(withConsumedEvent { me: MouseEvent =>
    val delta = calculateDelta(me)
    val isoCorner = getBottomLeft.asJava
    val boundHeight = getHeight
    val oldY = getTopRight.y
    val oldMiddle = untransformedMiddlePoint.asJava
    withCheckedBounds(getWidth+delta.getX,getHeight+delta.getY*(-1)) {
      if (adjustCoordinates) {
        setY(oldY - (getHeight - boundHeight))
        val newMiddle = untransformedMiddlePoint.asJava
        val deltaP = GeometryUtils.calculateRotationOffset(oldMiddle, newMiddle, getRotate, isoCorner)
        setX(getX + deltaP.getX)
        setY(getY + deltaP.getY)
      }
    }

    startMouse = (me.getSceneX,me.getSceneY)
  })

  private def untransformedMiddlePoint: Point =
    GeometryUtils.rectangleMiddlePoint(getTopLeft, getTopRight, getBottomLeft, getBottomRight)

  bottomRightAnchor.setOnMousePressed(withConsumedEvent { me: MouseEvent =>
    val oldHeight = getHeight
    val oldWidth = getWidth

    startMouse = (me.getSceneX, me.getSceneY)
    command = History.partialAction {
      setWidth(oldWidth)
      setHeight(oldHeight)
    }
  })

  bottomRightAnchor.setOnMouseDragged(withConsumedEvent { me: MouseEvent =>
    val delta = calculateDelta(me)
    val isoCorner = getTopLeft.asJava

    val oldMiddle = untransformedMiddlePoint.asJava
    withCheckedBounds(getWidth + delta.getX, getHeight + delta.getY) {
      val newMiddle = untransformedMiddlePoint.asJava
      val deltaP = GeometryUtils.calculateRotationOffset(oldMiddle, newMiddle, getRotate, isoCorner)

      if(adjustCoordinates) {
        setX(getX + deltaP.getX)
        setY(getY + deltaP.getY)
      }
    }

    startMouse = (me.getSceneX,me.getSceneY)
  })

  bottomLeftAnchor.setOnMousePressed(withConsumedEvent { me: MouseEvent =>
    val (oldX, oldY) = getTopLeft
    val oldHeight = getHeight
    val oldWidth = getWidth

    startMouse = (me.getSceneX, me.getSceneY)

    command = History.partialAction {
      if (adjustCoordinates) {
        setX(oldX)
      }
      setWidth(oldWidth)
      setHeight(oldHeight)
    }
  })

  bottomLeftAnchor.setOnMouseDragged(withConsumedEvent { me: MouseEvent =>
    val delta = calculateDelta(me)
    val isoCorner = getTopRight.asJava
    val boundWidth = getWidth
    val oldX = getBottomLeft.x
    val oldMiddle = untransformedMiddlePoint.asJava
    withCheckedBounds(getWidth+delta.getX*(-1),getHeight+delta.getY) {
      if (adjustCoordinates) {
        setX(oldX - (getWidth-boundWidth))
        val newMiddle = untransformedMiddlePoint.asJava
        val deltaP = GeometryUtils.calculateRotationOffset(oldMiddle, newMiddle, getRotate, isoCorner)
        setX(getX + deltaP.getX)
        setY(getY + deltaP.getY)
      }
    }

    startMouse = (me.getSceneX,me.getSceneY)
  })

  /** Calculates the delta-offset for a resize with the value `startMouse` and
    * the given `newMouse` MouseEvent.
    *
    * @note To use this function first set startMouse in the XXXMousePressed-handler
    *       and use this function in the XXXMouseDragged-handler.
    *       The coordinates of `startMouse` have to be relative to the scene!
    */
  private def calculateDelta(newMouse:MouseEvent):Point2D = {
    val transStart = sceneToLocal(startMouse.x, startMouse.y)
    val transEnd = sceneToLocal(newMouse.getSceneX,newMouse.getSceneY)
    transEnd.subtract(transStart)
  }
}
