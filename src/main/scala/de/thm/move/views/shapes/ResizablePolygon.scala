/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.views.shapes

import javafx.scene.shape.Polygon


import de.thm.move.types._

import scala.collection.JavaConverters._

class ResizablePolygon(val points:List[Double])
  extends Polygon(points:_*)
  with ResizableShape
  with ColorizableShape
  with QuadCurveTransformable
  with PathLike {

  override lazy val edgeCount: Int = points.size / 2
  override def toCurvedShape = QuadCurvePolygon(this)
  override def copy: ResizablePolygon = {
    val duplicate = new ResizablePolygon(getPoints.asScala.map(_.doubleValue).toList)
    duplicate.copyColors(this)
    duplicate.setRotate(getRotate)
    duplicate
  }

  private def pointIdxToListIdx(idx:Int):(Int,Int) = (idx * 2, idx * 2 +1)

  override def resize(idx: Int, delta: (Double, Double)): Unit = {
    val (xIdx,yIdx) = pointIdxToListIdx(idx)
    getPoints.set(xIdx, getPoints.get(xIdx)+delta.x)
    getPoints.set(yIdx, getPoints.get(yIdx)+delta.y)
  }

  override def getEdgePoint(idx: Int): (Double, Double) = {
    val (xIdx,yIdx) = pointIdxToListIdx(idx)
    (getPoints.get(xIdx), getPoints.get(yIdx))
  }
}

object ResizablePolygon {
  def apply(points:List[Point]):ResizablePolygon = {
    val singlePoints= points.flatMap { case (x,y) => List(x,y) }
    new ResizablePolygon(singlePoints)
  }

  def apply(cubed:QuadCurvePolygon):ResizablePolygon = {
    val polygon = ResizablePolygon(cubed.getUnderlyingPolygonPoints)
    polygon.colorizeShape(cubed.getFillColor, cubed.getStrokeColor)
    polygon.setStrokeWidth(cubed.getStrokeWidth)
    polygon
  }
}
