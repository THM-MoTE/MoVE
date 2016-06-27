/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
