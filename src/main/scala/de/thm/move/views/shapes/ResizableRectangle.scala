package de.thm.move.views.shapes

import javafx.scene.shape.Rectangle

import de.thm.move.models.CommonTypes.Point
import de.thm.move.views.Anchor

class ResizableRectangle(
            startPoint:Point,
            width:Double,
            height:Double) extends Rectangle(startPoint._1, startPoint._2, width, height) with ResizableShape with ColorizableShape {
  private val (x,y) = startPoint

  //create resize anchors
  private val topLeftAnchor = new Anchor(x,y)
  private val topRightAnchor = new Anchor(x+width,y)
  private val bottomLeftAnchor = new Anchor(x,y+height)
  private val bottomRightAnchor = new Anchor(x+width, y+height)
  val getAnchors: List[Anchor] = List(topLeftAnchor, topRightAnchor, bottomLeftAnchor, bottomRightAnchor)

  bindAnchorsTranslationToShapesLayout(this)(getAnchors:_*)
}
