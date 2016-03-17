package de.thm.move.views.shapes

import javafx.scene.shape.Shape

import de.thm.move.views.Anchor

trait ResizableShape {

  def getAnchors: List[Anchor]

  private[shapes] def bindAnchorsTranslationToShapesLayout(shape:Shape)(anchors:Anchor*): Unit = {
    anchors.foreach { anchor =>
      anchor.layoutXProperty().bind(shape.layoutXProperty())
      anchor.layoutYProperty().bind(shape.layoutYProperty())
    }
  }
}
