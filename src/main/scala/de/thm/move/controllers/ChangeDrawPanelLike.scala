package de.thm.move.controllers

import javafx.scene.Node

import de.thm.move.views.shapes.ResizableShape

trait ChangeDrawPanelLike {

  /**Adds the given element to the DrawPanel. Sets the inputHandlers for shapes.*/
  def addShape[T <: Node](shape:T*): Unit
  /**Removes the given shape with '''it's anchors''' from the DrawPanel*/
  def removeShape(shape:ResizableShape): Unit
}
