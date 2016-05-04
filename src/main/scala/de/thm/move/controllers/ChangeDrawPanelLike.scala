/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.controllers

import javafx.scene.Node

import de.thm.move.views.anchors.Anchor
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

  def addShapeWithAnchors(shape: ResizableShape): Unit = {
    addShape(shape)
    addNode(shape.getAnchors)
  }
  /** Adds a node to the panel. This method doesn't add handlers or context-menus.*/
  def addNode(node: Node*): Unit
  val addNode: List[Node] => Unit = toListParamFn(addNode _)
  /**Removes the given shape with '''it's anchors''' from the DrawPanel*/
  def removeShape(shape: ResizableShape): Unit
  def remove(n:Node): Unit
  def removeAll(): Unit = getElements foreach remove
  def getElements: List[Node]
  def contains(n:Node):Boolean = getElements.contains(n)

  def setVisibilityOfAnchors(flag:Boolean): Unit = {
    getElements.filter(_.isInstanceOf[Anchor]) foreach (  _.setVisible(flag) )
  }
}
