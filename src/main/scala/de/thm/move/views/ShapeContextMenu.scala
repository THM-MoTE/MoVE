/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.views

import javafx.scene.control.{ContextMenu, MenuItem}

class ShapeContextMenu extends ContextMenu {
  val inForegroundItem = new MenuItem("In foreground")
  val inBackgroundItem = new MenuItem("In background")
  val duplicateElementItem = new MenuItem("Duplicate")
  val resetRotationElementItem = new MenuItem("Reset rotation")
  val rotate90ClockwiseItem = new MenuItem("Rotate 90° clockwise")
  val rotate90CounterClockwiseItem = new MenuItem("Rotate 90° counter-clockwise")

  getItems.addAll(
    inForegroundItem,
    inBackgroundItem,
    duplicateElementItem,
    resetRotationElementItem,
    rotate90ClockwiseItem,
    rotate90CounterClockwiseItem)
}
