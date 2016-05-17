/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.controllers

import de.thm.move.views.shapes.ResizableShape

/** Base trait for all functions that work with selected elements */
trait SelectionCtrlLike {
  /** Returns the currently selected elements */
  def getSelectedShapes: List[ResizableShape]
}
