/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views.shapes

import javafx.scene.Node
import de.thm.move.views.Anchor
import de.thm.move.util.PointUtils._
import de.thm.move.models.CommonTypes._

trait ResizableShape extends Node with MovableShape {

  val selectionRectangle = new SelectionRectangle(this)

  val getAnchors: List[Anchor]

  def getX: Double
  def getY: Double
  final def getXY: Point = (getX, getY)

  def move(delta:Point):Unit = setXY(delta+getXY)
  def setX(x:Double): Unit
  def setY(y:Double): Unit
  final def setXY(p:Point): Unit = {
    setX(p._1)
    setY(p._2)
  }

  /** Creates a '''exact copy''' of this element. */
  def copy: ResizableShape
}
