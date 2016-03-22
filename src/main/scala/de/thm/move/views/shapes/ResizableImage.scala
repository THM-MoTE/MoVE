package de.thm.move.views.shapes

import javafx.scene.image.{ImageView, Image}
import javafx.scene.input.MouseEvent

import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.util.BindingUtils
import de.thm.move.models.CommonTypes._
import de.thm.move.views.Anchor

class ResizableImage(img:Image) extends ImageView(img) with ResizableShape with BoundedAnchors {
  setPreserveRatio(true)
  setFitWidth(200)
/*
  topLeftAnchor.setOnMouseDragged({me:MouseEvent =>
    val (newX, newY) = (me.getX, me.getY)
    val (oldX, oldY) = (this.getX, this.getY)
    val deltaY = if(newY < oldY) getFitHeight + (oldY-newY)else getFitHeight - (newY-oldY)
    val deltaX = if(newX > oldX) getFitWidth + (oldX-newX) else getFitWidth - (newX-oldX)
    this.setY(newY)
    this.setFitHeight(deltaY)
    this.setX(newX)
    this.setFitWidth(deltaX)
  })

  topRightAnchor.setOnMouseDragged({ me: MouseEvent =>
    val (newX, newY) = (me.getX, me.getY)
    val (oldX, oldY) = (this.getX+this.getFitWidth, this.getY)
    val deltaY = if(newY < oldY) getFitHeight + (oldY-newY) else getFitHeight - (newY-oldY)
    val deltaX = if(newX > oldX) getFitWidth + (newX-oldX) else getFitWidth - (oldX-newX)

    this.setY(newY)
    this.setFitHeight(deltaY)
    this.setFitWidth(deltaX)
  })

  bottomLeftAnchor.setOnMouseDragged({ me: MouseEvent =>
    val (newX, newY) = (me.getX, me.getY)
    val (oldX, oldY) = (this.getX, this.getY+getFitHeight)

    val deltaY = (newY-oldY) + getFitHeight
    val deltaX = (oldX-newX) + getFitWidth

    this.setX(newX)
    this.setFitHeight(deltaY)
    this.setFitWidth(deltaX)
  })

  bottomRightAnchor.setOnMouseDragged({ me: MouseEvent =>
    val (newX, newY) = (me.getX, me.getY)
    val (oldX, oldY) = (this.getX+this.getFitWidth, this.getY+this.getFitHeight)
    val deltaX = (newX-oldX) + this.getFitWidth
    val deltaY = (newY-oldY) + this.getFitWidth

    setFitWidth(deltaX)
    setFitHeight(deltaY)
  })

  */

  override def getTopLeft:Point = (getX, getY)
  override def getWidth: Double = getFitWidth
  override def getHeight: Double = getFitHeight

  override def setWidth(w:Double): Unit = setFitWidth(w)
  override def setHeight(h:Double): Unit = setFitHeight(h)

  BindingUtils.binAnchorsLayoutToNodeLayout(this)(getAnchors:_*)
}
