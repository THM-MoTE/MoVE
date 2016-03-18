package de.thm.move.util

import javafx.scene.shape.Shape

import de.thm.move.views.Anchor

object BindingUtils {
  def bindAnchorsTranslationToShapesLayout(shape:Shape)(anchors:Anchor*): Unit = {
    anchors.foreach { anchor =>
      anchor.layoutXProperty().bind(shape.layoutXProperty())
      anchor.layoutYProperty().bind(shape.layoutYProperty())
    }
  }
}
