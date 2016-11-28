/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.views.shapes

import javafx.scene.shape.Rectangle
import de.thm.move.types._


class ResizableRectangle(
            startPoint:Point,
            width:Double,
            height:Double)
    extends Rectangle(startPoint._1, startPoint._2, width, height)
    with ResizableShape
    with RectangleLike
    with ColorizableShape {
  private val (x,y) = startPoint

  override def getTopLeft:Point = (getX, getY)
  override def getTopRight:Point = (getX + getWidth, getY)
  override def getBottomLeft:Point = (getX, getY + getHeight)
  override def getBottomRight:Point = (getX + getWidth,getY + getHeight)
  override def copy: ResizableRectangle = {
    val duplicate = new ResizableRectangle(startPoint, width, height)
    duplicate.copyColors(this)
    duplicate.copyPosition(this)
    duplicate
  }
}
