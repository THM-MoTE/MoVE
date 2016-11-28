/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.views.shapes



import scala.collection.JavaConverters._
import de.thm.move.types._
/**
 * A polygon with quadratic Bezier curves as edge points.
 */
class QuadCurvePolygon(points:List[Point])
  extends AbstractQuadCurveShape(points, true) {
  override def toUncurvedShape: ResizablePolygon = ResizablePolygon(this)
  override def copy: QuadCurvePolygon = {
    val duplicate = new QuadCurvePolygon(getUnderlyingPolygonPoints)
    duplicate.copyColors(this)
    duplicate.setRotate(getRotate)
    duplicate
  }
}

object QuadCurvePolygon {
  def apply(points:List[Double]):QuadCurvePolygon = {
    val singlePoints=
      for(idx <- 0 until points.size by 2)
      yield ( points(idx),points(idx+1) )
    new QuadCurvePolygon(singlePoints.toList)
  }

  def apply(polygon:ResizablePolygon):QuadCurvePolygon = {
    val points = polygon.getPoints.asScala.map(_.doubleValue).toList
    val curvedPolygon = QuadCurvePolygon(points)
    curvedPolygon.copyColors(polygon)
    curvedPolygon.setRotate(polygon.getRotate)
    curvedPolygon
  }
}
