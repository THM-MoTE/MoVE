package de.thm.move.views.shapes

import javafx.scene.image.{ImageView, Image}
import javafx.scene.input.MouseEvent

import de.thm.move.controllers.implicits.FxHandlerImplicits._

import de.thm.move.views.Anchor

class ResizableImage(img:Image) extends ImageView(img) with ResizableShape with BoundedAnchors {
  setPreserveRatio(true)
  setFitWidth(200)

  bottomRightAnchor.setOnMouseDragged({ me: MouseEvent =>
    val (newX, newY) = (me.getX, me.getY)
    val (oldX, oldY) = (this.getX+this.getFitWidth, this.getY+this.getFitHeight)
    val deltaX = (newX-oldX) + this.getFitWidth
    val deltaY = (newY-oldY) + this.getFitWidth

    setFitWidth(deltaX)
    setFitHeight(deltaY)
  })

  override val getAnchors: List[Anchor] = List(topLeftAnchor, topRightAnchor, bottomLeftAnchor, bottomRightAnchor)
}
