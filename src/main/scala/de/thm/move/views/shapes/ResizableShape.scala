/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.views.shapes

import javafx.scene.Node

import de.thm.move.models.CommonTypes._
import de.thm.move.views.anchors.Anchor

/** Base trait for all shapes. */
trait ResizableShape extends Node with MovableShape with RotatableShape {
  /** The dotted rectangle around this shape for highlighting the current selected shape. */
  val selectionRectangle = new SelectionRectangle(this)
  /** The anchors for resizing the shape at the edge-points of this shape. */
  val getAnchors: List[Anchor]
  /** Creates a '''exact copy''' of this element. */
  def copy: ResizableShape

  /** Converts the given point from local-coordinate space into parent's coordinate space.
    *
    * @see [[javafx.scene.Node]]
    * */
  def localToParentPoint(point:Point):Point = {
    val (x,y) = point
    val point2D = localToParent(x,y)
    (point2D.getX, point2D.getY)
  }
}
