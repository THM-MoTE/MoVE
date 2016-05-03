/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views.shapes

import de.thm.move.models.CommonTypes.Point
import de.thm.move.views.Anchor
import scala.collection.JavaConverters._

/**
 * A polygon with quadratic Bezier curves as edge points.
 */
class QuadCurvePolygon(points:List[Point])
  extends AbstractQuadCurveShape(points, true) {
  override def toUncurvedShape: ResizableShape = ResizablePolygon(this)
  override def copy: ResizableShape = {
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
