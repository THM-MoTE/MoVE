package de.thm.move.views.shapes

import javafx.scene.shape.{Ellipse, Circle}

import de.thm.move.models.CommonTypes._
import de.thm.move.views.Anchor

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

  radiusXProperty().bind(bottomRightAnchor.centerXProperty().subtract(width).divide(2))
  radiusYProperty().bind(bottomRightAnchor.centerYProperty().subtract(height).divide(2))

  val getAnchors: List[Anchor] = List(topLeftAnchor, topRightAnchor, bottomLeftAnchor, bottomRightAnchor)

  bindAnchorsTranslationToShapesLayout(this)(getAnchors:_*)
}
