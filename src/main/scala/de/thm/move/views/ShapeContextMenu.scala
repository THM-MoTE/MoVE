package de.thm.move.views

import javafx.scene.Node
import javafx.scene.control.{MenuItem, ContextMenu}

class ShapeContextMenu extends ContextMenu {
  val inForegroundItem = new MenuItem("In foreground")
  val inBackgroundItem = new MenuItem("In background")
  getItems.addAll(inForegroundItem, inBackgroundItem)
}
