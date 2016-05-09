/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views.shapes

import de.thm.move.models.CommonTypes._

/** A shape that is movable.
  *
  * @note Implementations should move an element by moving the shapes specific points.
  * */
trait MovableShape {
  /** Moves this shape by the given delta */
  def move(delta:Point):Unit
}
