/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views.shapes

import de.thm.move.models.CommonTypes.Point
import javafx.scene.paint.Paint

import de.thm.move.views.anchors.Anchor

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
