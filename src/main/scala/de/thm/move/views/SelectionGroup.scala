package de.thm.move.views

import javafx.scene.Group
import de.thm.move.views.shapes.MovableShape
import de.thm.move.views.shapes.ResizableShape
import de.thm.move.models.CommonTypes._
import de.thm.move.views.shapes.SelectionRectangle

class SelectionGroup(children:List[ResizableShape]) extends Group with ResizableShape {
  
  getStyleClass.addAll("selection-rectangle")
  setAutoSizeChildren(false)
  getChildren().addAll(children:_*)

  val getAnchors: List[Anchor] = Nil

  def getX: Double = getBoundsInLocal().getMinX
  def getY: Double = getBoundsInLocal().getMinY
  def setX(x:Double): Unit = throw new UnsupportedOperationException()
  def setY(y:Double): Unit = throw new UnsupportedOperationException()
  def copy: ResizableShape = ???

  override def move(delta:Point):Unit = children.foreach(_.move(delta))
}
