/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views

import javafx.scene.Node

import de.thm.move.views.shapes.{ResizableShape, SelectionRectangle}

/** Represents a group of elements. */
trait GroupLike extends Node {
  /** A highlighted selection around all child elements */
  val selectionRectangle:SelectionRectangle
  /** Returns the components inside this group */
  def childrens: List[ResizableShape]
}
