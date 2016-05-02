/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views.shapes

import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import de.thm.move.util.JFxUtils
import de.thm.move.views.{MovableAnchor, Anchor}

import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.models.CommonTypes.Point
import de.thm.move.util.PointUtils._
import scala.collection.JavaConversions._

class ResizablePolygon(val points:List[Double])
  extends Polygon(points:_*)
  with ResizableShape
  with ColorizableShape
  with QuadCurveTransformable
  with PathLike {

  override val edgeCount: Int = points.size / 2
  override val getAnchors: List[Anchor] = genAnchors
  override def toCurvedShape = QuadCurvePolygon(this)
  override def copy: ResizableShape = {
    val duplicate = new ResizablePolygon(getPoints.map(_.doubleValue).toList)
    duplicate.copyColors(this)
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
