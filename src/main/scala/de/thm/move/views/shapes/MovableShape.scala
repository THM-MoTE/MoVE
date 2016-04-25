/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views.shapes

import de.thm.move.models.CommonTypes._

trait MovableShape {
  def move(delta:Point):Unit
}
