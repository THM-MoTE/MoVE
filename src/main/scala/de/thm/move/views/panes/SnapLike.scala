/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.views.panes

import javafx.scene.Node

import de.thm.move.types._
import de.thm.move.views.shapes.MovableShape

/** Represents elements that can calculate a distance for a snapping-mechanism to a specific element.
  *
  * E.G.: A grid with lines on which elements can get snapped (a.k.a. snap-to-grid).
  * */
trait SnapLike {
  /** Returns the x-coordinate for the closest element to deltaX. */
  def getClosestXPosition(deltaX:Double): Option[Int]
  /** Returns the y-coordinate for the closest element to deltaY. */
  def getClosestYPosition(deltaY:Double): Option[Int]

  /** Returns the point for the closest element to p. */
  def getClosestPosition(p:Point):Option[Point] = {
    val xOpt = getClosestXPosition(p._1)
    val yOpt = getClosestYPosition(p._2)
    for {
      x <- xOpt
      y <- yOpt
    } yield (x.toDouble,y.toDouble)
  }
}

object SnapLike {
  /** Applys snap-t-grid to the given node using node's boundsInParent-property. */
  def applySnapToGrid(snaplike:SnapLike, node:Node with MovableShape): Unit = {
    val delta = getSnapToGridDistance(snaplike, node.getBoundsInParent.getMinX,
      node.getBoundsInParent.getMinY)

    node.move(delta)
  }

  /** Returns the delta for snap-to-grid for the point represented by (x,y). */
  def getSnapToGridDistance(snaplike:SnapLike, x:Double,y:Double):Point = {
    val deltaX = snaplike.getClosestXPosition(x).
      map (_.toDouble - x).getOrElse(0.0)

    val deltaY = snaplike.getClosestYPosition(y).
      map (_.toDouble - y).getOrElse(0.0)

    (deltaX,deltaY)
  }
}