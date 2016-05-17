/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views

import javafx.scene.Group

import de.thm.move.models.CommonTypes._
import de.thm.move.views.anchors.Anchor
import de.thm.move.views.shapes._

/** A group of resizable shapes used for the grouping-mechanism (Edit - Group Elements) */
class SelectionGroup(children:List[ResizableShape])
    extends Group
    with GroupLike
    with ResizableShape {
  getChildren.addAll(children:_*)

  val getAnchors: List[Anchor] = children.flatMap(_.getAnchors)

  def copy: ResizableShape = {
    val childCopies = children.map(_.copy)
    new SelectionGroup(childCopies)
  }

  override def move(delta:Point):Unit = children.foreach(_.move(delta))
  override def childrens: List[ResizableShape] = children
}
