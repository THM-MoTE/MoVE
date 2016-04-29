/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views

/** Turns a anchor into a rotation-anchor */
trait RotateAnchor {
  this: Anchor =>
  this.setId(DrawPanel.tmpShapeId)
  this.getStyleClass.addAll("rotation-anchor")
}
