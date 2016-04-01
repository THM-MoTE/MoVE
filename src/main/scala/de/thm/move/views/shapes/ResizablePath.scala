package de.thm.move.views.shapes

import javafx.scene.paint.Paint
import javafx.scene.shape.{PathElement, Path}
import collection.JavaConversions._

import de.thm.move.views.Anchor

class ResizablePath(elements:Seq[PathElement]) extends Path(elements) with ResizableShape with ColorizableShape {
  override def getAnchors: List[Anchor] = Nil

  override def setY(y: Double): Unit = setLayoutY(y)

  override def getY: Double = getLayoutY

  override def setX(x: Double): Unit = setLayoutX(x)

  override def getX: Double = getLayoutX

  override def getFillColor:Paint = null
  override def setFillColor(c:Paint):Unit = Unit
}