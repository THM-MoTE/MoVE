/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views

import javafx.scene.control.{ContextMenu, MenuItem}

class ShapeContextMenu extends ContextMenu {
  val inForegroundItem = new MenuItem("In foreground")
  val inBackgroundItem = new MenuItem("In background")
  val duplicateElementItem = new MenuItem("Duplicate")
  val resetRotationElementItem = new MenuItem("Reset rotation")
  getItems.addAll(
    inForegroundItem,
    inBackgroundItem,
    duplicateElementItem,
    resetRotationElementItem)
}
