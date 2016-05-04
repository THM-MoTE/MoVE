/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views

import de.thm.move.views.panes.DrawPanel

/** Turns a anchor into a rotation-anchor */
trait RotateAnchor {
  this: anchors.Anchor =>
  this.setId(DrawPanel.tmpShapeId)
  this.getStyleClass.addAll("rotation-anchor")
}
