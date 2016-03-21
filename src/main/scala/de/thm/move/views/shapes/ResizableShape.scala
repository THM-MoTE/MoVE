package de.thm.move.views.shapes

import de.thm.move.views.Anchor

trait ResizableShape {
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
