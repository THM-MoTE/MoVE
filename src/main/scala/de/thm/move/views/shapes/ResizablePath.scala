package de.thm.move.views.shapes

import javafx.scene.paint.Paint
import javafx.scene.shape.{MoveTo, LineTo, PathElement, Path}
import de.thm.move.util.BindingUtils
import javafx.scene.paint.Color

import collection.JavaConversions._

import de.thm.move.views.{MovableAnchor, Anchor}

class ResizablePath(startPoint: MoveTo, elements:List[LineTo]) extends Path(startPoint :: elements) with ResizableShape with ColorizableShape {

  val alElements = startPoint :: elements

  override val getAnchors: List[Anchor] =
    alElements.flatMap {
      case move:MoveTo =>
        val anchor = new Anchor(move.getX,move.getY) with MovableAnchor
        move.xProperty.bind(anchor.centerXProperty)
        move.yProperty.bind(anchor.centerYProperty)
        List(anchor)
      case line:LineTo =>
        val anchor = new Anchor(line.getX,line.getY) with MovableAnchor
        line.xProperty.bind(anchor.centerXProperty)
        line.yProperty.bind(anchor.centerYProperty)

        List(anchor)
    }

  BindingUtils.binAnchorsLayoutToNodeLayout(this)(getAnchors:_*)

  override def setY(y: Double): Unit = setLayoutY(y)

  override def getY: Double = getLayoutY

  override def setX(x: Double): Unit = setLayoutX(x)

  override def getX: Double = getLayoutX

  override def getFillColor:Paint = null /*Path has no fill*/
  override def setFillColor(c:Paint):Unit = { /*Path has no fill*/ }
}
