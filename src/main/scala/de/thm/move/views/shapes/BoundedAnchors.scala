package de.thm.move.views.shapes

import javafx.geometry.Bounds
import javafx.scene.input.MouseEvent
import javafx.scene.shape.Ellipse
import javafx.scene.Node

import de.thm.move.models.CommonTypes._
import de.thm.move.views.Anchor
import de.thm.move.controllers.implicits.FxHandlerImplicits._

trait BoundedAnchors {
  self: ResizableShape =>

  //resize anchors at edges
  val topLeftAnchor = new Anchor(getTopLeft)
  val topRightAnchor = new Anchor(getTopRight)
  val bottomLeftAnchor = new Anchor(getBottomLeft)
  val bottomRightAnchor = new Anchor(getBottomRight)
  override val getAnchors: List[Anchor] = List(topLeftAnchor, topRightAnchor, bottomLeftAnchor, bottomRightAnchor)

  def getTopLeft:Point = (getBoundsInLocal.getMinX, getBoundsInLocal.getMinY)
  def getTopRight:Point = (getBoundsInLocal.getMaxX, getBoundsInLocal.getMinY)
  def getBottomLeft:Point = (getBoundsInLocal.getMinX, getBoundsInLocal.getMaxY)
  def getBottomRight:Point = (getBoundsInLocal.getMaxX,getBoundsInLocal.getMaxY)

  def adjustCenter(e:Ellipse, newPoint:Point): Unit = {
    val (x,y) = newPoint
    e.setCenterX(x)
    e.setCenterY(y)
  }

  //adjust the anchors to the bounding-box
  boundsInLocalProperty().addListener({ (_:Bounds, _:Bounds) =>
    adjustCenter(topLeftAnchor, getTopLeft)
    adjustCenter(topRightAnchor, getTopRight)
    adjustCenter(bottomLeftAnchor, getBottomLeft)
    adjustCenter(bottomRightAnchor, getBottomRight)
  })

  topLeftAnchor.setOnMouseDragged ({ me: MouseEvent =>
    val (oldX, oldY) = getTopLeft
    val (newX, newY) = (me.getX, me.getY)
    val boundWidth = getWidth
    val boundHeight = getHeight

    val deltaX = if(oldX > newX) ((oldX-newX) + boundWidth) else (boundWidth - (newX-oldX))
    val deltaY = if(newY < oldY) ((oldY - newY)  + boundHeight) else (boundHeight - (newY-oldY))

    setWidth(deltaX)
    setHeight(deltaY)
  })

  topRightAnchor.setOnMouseDragged({ me: MouseEvent =>
    val (oldX, oldY) = getTopRight
    val (newX, newY) = (me.getX, me.getY)

    val boundWidth = getWidth
    val boundHeight = getHeight

    val deltaX = if(newX>oldX) ((newX - oldX) + boundWidth) else (boundWidth-(oldX-newX))
    val deltaY = if(newY < oldY) (oldY - newY  + boundHeight) else (boundHeight - (newY-oldY))

    setWidth(deltaX)
    setHeight(deltaY)

  })

  bottomRightAnchor.setOnMouseDragged({ me: MouseEvent =>
    val (oldX, oldY) = getBottomRight
    val (newX, newY) = (me.getX, me.getY)

    val boundWidth = getWidth
    val boundHeight = getHeight

    val deltaX = (newX - oldX + boundWidth)
    val deltaY = (newY - oldY + boundHeight)

    setWidth(deltaX)
    setHeight(deltaY)
  })

  bottomLeftAnchor.setOnMouseDragged({ me: MouseEvent =>
    val (oldX, oldY) = getBottomLeft
    val (newX, newY) = (me.getX, me.getY)

    val boundWidth = getWidth
    val boundHeight = getHeight

    val deltaX = if(newX<oldX) ((oldX-newX) + boundWidth) else (boundWidth-(newX-oldX))
    val deltaY = if(newY > oldY) ((newY-oldY)+boundHeight) else (boundHeight-(oldY-newY))

    setWidth(deltaX)
    setHeight(deltaY)
  })
}
