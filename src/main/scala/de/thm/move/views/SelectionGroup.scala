/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views

import javafx.scene.Group
import javafx.scene.paint.Paint
import de.thm.move.views.shapes._
import de.thm.move.models.CommonTypes._
import de.thm.move.controllers.implicits.FxHandlerImplicits._
import javafx.scene.layout.Pane
import javafx.scene.input.MouseEvent

/** A group of resizable shapes used for the grouping-mechanism (Edit - Group Elements) */
class SelectionGroup(children:List[ResizableShape])
    extends Group
    with GroupLike
    with ResizableShape {
  getChildren.addAll(children:_*)

  val getAnchors: List[Anchor] = children.flatMap(_.getAnchors)

  def copy: ResizableShape = throw new UnsupportedOperationException("Not supported")

  override def move(delta:Point):Unit = children.foreach(_.move(delta))
  override def childrens: List[ResizableShape] = children
}
