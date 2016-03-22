package de.thm.move.views.shapes

import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.shape.Rectangle
import javafx.geometry.Bounds
import javafx.beans.value.{ChangeListener, ObservableValue}
import de.thm.move.controllers.implicits.FxHandlerImplicits._

class SelectionRectangle(selectedShape:ResizableShape) extends Rectangle {
  import SelectionRectangle._

  val xProp = new SimpleDoubleProperty(selectedShape.getBoundsInLocal().getMinX)
  val yProp = new SimpleDoubleProperty(selectedShape.getBoundsInLocal().getMinY)
  val widthProp = new SimpleDoubleProperty(selectedShape.getBoundsInLocal().getWidth)
  val heightProp = new SimpleDoubleProperty(selectedShape.getBoundsInLocal().getHeight)

  selectedShape.layoutBoundsProperty().addListener { (_:Bounds, newBounds:Bounds) =>
    xProp.set( newBounds.getMinX )
    yProp.set( newBounds.getMinY )
    widthProp.set( newBounds.getWidth )
    heightProp.set( newBounds.getHeight )
  }

  this.getStyleClass.addAll("selection-rectangle")

  this.xProperty().bind(xProp.subtract(distanceToShape))
  this.yProperty().bind(yProp.subtract(distanceToShape))
  this.widthProperty().bind(widthProp.add(distanceToShape).add(8))
  this.heightProperty().bind(heightProp.add(distanceToShape).add(8))
}

object SelectionRectangle {
  val distanceToShape = 10
}
