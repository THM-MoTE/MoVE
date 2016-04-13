package de.thm.move.views.shapes

import de.thm.move.models.CommonTypes._

trait MovableShape {
  def move(delta:Point):Unit
}
