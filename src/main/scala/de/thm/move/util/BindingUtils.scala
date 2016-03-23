/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.util

import javafx.scene.shape.Shape

import de.thm.move.views.Anchor
import javafx.scene.Node


object BindingUtils {
  def binAnchorsLayoutToNodeLayout(node:Node)(anchors:Anchor*): Unit = {
    anchors.foreach { anchor =>
      anchor.layoutXProperty().bind(node.layoutXProperty())
      anchor.layoutYProperty().bind(node.layoutYProperty())
    }
  }
}
