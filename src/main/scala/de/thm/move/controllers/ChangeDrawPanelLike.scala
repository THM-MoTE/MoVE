package de.thm.move.controllers

import javafx.scene.Node

import de.thm.move.views.shapes.ResizableShape

trait ChangeDrawPanelLike {

  /**
   * Converts a function with a vararg array as parameter into a function
   * that  takes a list as parameter.
   */
  private def toListParamFn[A, B](fn: (A*) => B): List[A] => B = fn(_: _*)

  /**
   * Adds the given element to the DrawPanel.
   * Sets the inputHandlers & context-menu for shapes.
   */
  def addShape(shape: ResizableShape*): Unit
  val addShape: List[ResizableShape] => Unit = toListParamFn(addShape _)
  /** Adds a node to the panel. This method doesn't add handlers or context-menus.*/
  def addNode(node: Node*): Unit
  val addNode: List[Node] => Unit = toListParamFn(addNode _)
  /**Removes the given shape with '''it's anchors''' from the DrawPanel*/
  def removeShape(shape: ResizableShape): Unit
}
