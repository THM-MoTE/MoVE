package de.thm.move.views

import javafx.scene.Node

import de.thm.move.views.shapes.{ResizableShape, SelectionRectangle}

trait GroupLike extends Node {
  val selectionRectangle:SelectionRectangle
  def childrens: List[ResizableShape]
}
