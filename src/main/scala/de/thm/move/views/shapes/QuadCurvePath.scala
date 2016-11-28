/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.views.shapes

import javafx.scene.paint.Paint
import de.thm.move.types._


class QuadCurvePath(points:List[Point])
  extends AbstractQuadCurveShape(points, false) {
  override def getFillColor:Paint = null /*Path has no fill*/
  override def setFillColor(c:Paint):Unit = { /*Path has no fill*/ }

  override def toUncurvedShape: ResizablePath = ResizablePath(this)
  override def copy: QuadCurvePath = {
    val duplicate = new QuadCurvePath(getUnderlyingPolygonPoints)
    duplicate.copyColors(this)
    duplicate.setRotate(getRotate)
    duplicate
  }
}

object QuadCurvePath {
  def apply(rePath:ResizablePath):QuadCurvePath = {
    val points:List[Point] = rePath.getPoints
    val path = new QuadCurvePath(points)
    path.copyColors(rePath)
    path.setRotate(rePath.getRotate)
    path
  }
  def apply(points:List[Point]) = new QuadCurvePath(points)
}
