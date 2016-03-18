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

  BindingUtils.bindAnchorsTranslationToShapesLayout(this)(getAnchors:_*)
}
