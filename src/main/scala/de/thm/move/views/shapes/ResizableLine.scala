package de.thm.move.views.shapes

import javafx.scene.shape.Line

import de.thm.move.models.CommonTypes.Point
import de.thm.move.util.BindingUtils
import de.thm.move.views.{MovableAnchor, Anchor}

class ResizableLine(
         start:Point,
         end:Point,
         strokeSize:Int) extends Line(start._1, start._2, end._1, end._2) with ResizableShape with ColorizableShape {
  setStrokeWidth(strokeSize)

  val startAnchor = new Anchor(start) with de.thm.move.views.MovableAnchor
  val endAnchor = new Anchor(end) with de.thm.move.views.MovableAnchor
  val getAnchors: List[Anchor] = List(startAnchor, endAnchor)

  BindingUtils.binAnchorsLayoutToNodeLayout(this)(getAnchors:_*)

  startXProperty().bind(startAnchor.centerXProperty())
  startYProperty().bind(startAnchor.centerYProperty())
  endXProperty().bind(endAnchor.centerXProperty())
  endYProperty().bind(endAnchor.centerYProperty())

  override def getX: Double = getLayoutX

  override def setY(y: Double): Unit = setLayoutY(y)

  override def getY: Double = getLayoutY

  override def getHeight: Double = getStrokeWidth

  override def getWidth: Double = getStrokeWidth

  override def setX(x: Double): Unit = setLayoutX(x)

  override def setWidth(w: Double): Unit = setStrokeWidth(w)

  override def setHeight(h: Double): Unit = setStrokeWidth(h)
}
