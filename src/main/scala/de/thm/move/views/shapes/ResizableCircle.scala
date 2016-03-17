package de.thm.move.views.shapes

import javafx.scene.shape.{Ellipse, Circle}

import de.thm.move.models.CommonTypes._
import de.thm.move.views.Anchor

class ResizableCircle(
        point:Point,
        width:Double,
        height:Double) extends Ellipse(point._1, point._2, width, height) with ColorizableShape {
  private val (x,y) = point

  val anchor = new Anchor(x,y)
  radiusXProperty().bind(anchor.centerXProperty())
  radiusYProperty().bind(anchor.centerYProperty())

  val getAnchors: List[Anchor] = List(anchor)
}
