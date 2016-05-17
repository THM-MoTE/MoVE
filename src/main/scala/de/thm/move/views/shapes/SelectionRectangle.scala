/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views.shapes

import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Bounds
import javafx.scene.shape.Rectangle

import de.thm.move.controllers.implicits.FxHandlerImplicits._

/** A rectangle that highlights the underlying shape by drawing a dotted-line around the underlying shape. */
class SelectionRectangle(selectedShape:ResizableShape) extends Rectangle {
  import SelectionRectangle._

  /* If underlying element (selectedShape) gets adjusted,
   * adjust the selection too.
   */
  val xProp = new SimpleDoubleProperty(selectedShape.getBoundsInLocal().getMinX)
  val yProp = new SimpleDoubleProperty(selectedShape.getBoundsInLocal().getMinY)
  val widthProp = new SimpleDoubleProperty(selectedShape.getBoundsInLocal().getWidth)
  val heightProp = new SimpleDoubleProperty(selectedShape.getBoundsInLocal().getHeight)

  selectedShape.boundsInParentProperty().addListener { (_:Bounds, newBounds:Bounds) =>
    xProp.set( newBounds.getMinX )
    yProp.set( newBounds.getMinY )
    widthProp.set( newBounds.getWidth )
    heightProp.set( newBounds.getHeight )
  }

  this.getStyleClass.addAll("selection-rectangle")

  this.xProperty().bind(xProp.subtract(distanceToShape))
  this.yProperty().bind(yProp.subtract(distanceToShape))
  this.widthProperty().bind(widthProp.add(distanceToShape).add(aditionalSpace))
  this.heightProperty().bind(heightProp.add(distanceToShape).add(aditionalSpace))
}

object SelectionRectangle {
  val distanceToShape = 5
  //needed for width, height because of some strange boundsInLocal behaviour
  val aditionalSpace = 6
}
