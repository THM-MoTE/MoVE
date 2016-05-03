/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views.shapes

import javafx.scene.Node
import de.thm.move.views.Anchor
import de.thm.move.util.PointUtils._
import de.thm.move.models.CommonTypes._

/** Base trait for all shapes. */
trait ResizableShape extends Node with MovableShape with RotatableShape {
  /** The dotted rectangle around this shape for highlighting the current selected shape. */
  val selectionRectangle = new SelectionRectangle(this)
  /** The anchors for resizing the shape at the edge-points of this shape. */
  val getAnchors: List[Anchor]
  /** Creates a '''exact copy''' of this element. */
  def copy: ResizableShape

  def localToParentPoint(point:Point):Point = {
    val (x,y) = point
    val point2D = localToParent(x,y)
    (point2D.getX, point2D.getY)
  }
}
