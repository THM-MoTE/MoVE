package de.thm.move.views.shapes

import javafx.scene.Node

import de.thm.move.views.Anchor

trait ResizableShape extends Node {

  val selectionRectangle = new SelectionRectangle(this)

  def getAnchors: List[Anchor]

  def getX: Double
  def getY: Double
  def getWidth: Double
  def getHeight: Double

  def setX(x:Double): Unit
  def setY(y:Double): Unit
  def setWidth(w:Double): Unit
  def setHeight(h:Double): Unit
}
