package de.thm.move.views.shapes

import javafx.geometry.Bounds
import javafx.scene.shape.Ellipse
import javafx.scene.Node

import de.thm.move.models.CommonTypes._
import de.thm.move.views.Anchor
import de.thm.move.controllers.implicits.FxHandlerImplicits._

trait BoundedAnchors {
  self: Node =>

  //resize anchors at edges
  val topLeftAnchor = new Anchor(getTopLeft)
  val topRightAnchor = new Anchor(getTopRight)
  val bottomLeftAnchor = new Anchor(getBottomLeft)
  val bottomRightAnchor = new Anchor(getBottomRight)

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
}
