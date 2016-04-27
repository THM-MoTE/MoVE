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

  @deprecated("Will be moved into RectangleLike", "2016-04-27")
  def getX: Double
  @deprecated("Will be moved into RectangleLike", "2016-04-27")
  def getY: Double
  @deprecated("Will be moved into RectangleLike", "2016-04-27")
  final def getXY: Point = (getX, getY)

  def move(delta:Point):Unit = setXY(delta+getXY)
  @deprecated("Will be moved into RectangleLike", "2016-04-27")
  def setX(x:Double): Unit
  @deprecated("Will be moved into RectangleLike", "2016-04-27")
  def setY(y:Double): Unit
  @deprecated("Will be moved into RectangleLike", "2016-04-27")
  final def setXY(p:Point): Unit = {
    setX(p._1)
    setY(p._2)
  }

  /** Creates a '''exact copy''' of this element. */
  def copy: ResizableShape
}
