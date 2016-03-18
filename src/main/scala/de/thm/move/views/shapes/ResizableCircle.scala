package de.thm.move.views.shapes

import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.shape.Ellipse
import javafx.geometry.Bounds

import de.thm.move.models.CommonTypes._
import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.views.Anchor
import javafx.scene.input.MouseEvent

class ResizableCircle(
        point:Point,
        width:Double,
        height:Double) extends Ellipse(point._1, point._2, width, height) with ResizableShape with ColorizableShape {
  private val (x,y) = point

  val minX = getBoundsInLocal.getMinX
  val minY = getBoundsInLocal.getMinY
  val maxX = getBoundsInLocal.getMaxX
  val maxY = getBoundsInLocal.getMaxY
  val topLeftAnchor = new Anchor(minX,minY)
  val topRightAnchor = new Anchor(minX+width*2, minY)
  val bottomLeftAnchor = new Anchor(minX, minY+height*2)
  val bottomRightAnchor = new Anchor(maxX, maxY)

  boundsInLocalProperty().addListener({ (oldBounds:Bounds, newBounds:Bounds) =>
    val (oldX,oldY) = (oldBounds.getMinX, oldBounds.getMinY)
    val (oldWidth, oldHeight) = (oldBounds.getWidth, oldBounds.getHeight)
    val (newX, newY) = (newBounds.getMinX, newBounds.getMinY)
    val (newWidth, newHeight) = (newBounds.getWidth, newBounds.getHeight)

    //adjust topLeft
    topLeftAnchor.setCenterX(getTopLeft._1)
    topLeftAnchor.setCenterY(getTopLeft._2)

    //adjust topRight
    topRightAnchor.setCenterX(getTopRight._1)
    topRightAnchor.setCenterY(getTopRight._2)

    //adjust bottomLeft
    bottomLeftAnchor.setCenterX(getBottomLeft._1)
    bottomLeftAnchor.setCenterY(getBottomLeft._2)

    //adjust bottomRight
    bottomRightAnchor.setCenterX(getBottomRight._1)
    bottomRightAnchor.setCenterY(getBottomRight._2)
  })

  def getTopLeft:Point = (getBoundsInLocal.getMinX, getBoundsInLocal.getMinY)
  def getTopRight:Point = (getBoundsInLocal.getMaxX, getBoundsInLocal.getMinY)
  def getBottomLeft:Point = (getBoundsInLocal.getMinX, getBoundsInLocal.getMaxY)
  def getBottomRight:Point = (getBoundsInLocal.getMaxX,getBoundsInLocal.getMaxY)

  topLeftAnchor.setOnMouseDragged ({ me: MouseEvent =>
    val (oldX, oldY) = getTopLeft
    val (newX, newY) = (me.getX, me.getY)
    val boundWidth = getBoundsInLocal.getWidth
    val boundHeight = getBoundsInLocal.getHeight

    val deltaX = if(oldX > newX) ((oldX-newX) + boundWidth) / 2 else (boundWidth - (newX-oldX)) / 2
    val deltaY = if(newY < oldY) ((oldY - newY)  + boundHeight) / 2 else (boundHeight - (newY-oldY)) / 2

    setRadiusX(deltaX)
    setRadiusY(deltaY)
  })

  topRightAnchor.setOnMouseDragged({ me: MouseEvent =>
    val (oldX, oldY) = getTopRight
    val (newX, newY) = (me.getX, me.getY)

    val boundWidth = getBoundsInLocal.getWidth
    val boundHeight = getBoundsInLocal.getHeight

    val deltaX = if(newX>oldX) ((newX - oldX) + boundWidth) / 2 else (boundWidth-(oldX-newX)) / 2
    val deltaY = if(newY < oldY) (oldY - newY  + boundHeight) / 2 else (boundHeight - (newY-oldY)) / 2

    setRadiusX(deltaX)
    setRadiusY(deltaY)

  })

  bottomRightAnchor.setOnMouseDragged({ me: MouseEvent =>
    val (oldX, oldY) = getBottomRight
    val (newX, newY) = (me.getX, me.getY)

    val boundWidth = getBoundsInLocal.getWidth
    val boundHeight = getBoundsInLocal.getHeight

    val deltaX = (newX - oldX + boundWidth) / 2
    val deltaY = (newY - oldY + boundHeight) / 2

    setRadiusX(deltaX)
    setRadiusY(deltaY)
  })

  bottomLeftAnchor.setOnMouseDragged({ me: MouseEvent =>
    val (oldX, oldY) = getBottomLeft
    val (newX, newY) = (me.getX, me.getY)

    val boundWidth = getBoundsInLocal.getWidth
    val boundHeight = getBoundsInLocal.getHeight

    val deltaX = if(newX<oldX) ((oldX-newX) + boundWidth)/2 else (boundWidth-(newX-oldX))/2
    val deltaY = if(newY > oldY) ((newY-oldY)+boundHeight)/2 else (boundHeight-(oldY-newY))/2

    setRadiusX(deltaX)
    setRadiusY(deltaY)
  })

  val getAnchors: List[Anchor] = List(topLeftAnchor, topRightAnchor, bottomLeftAnchor, bottomRightAnchor)

  //bindAnchorsTranslationToShapesLayout(this)(getAnchors:_*)
}
