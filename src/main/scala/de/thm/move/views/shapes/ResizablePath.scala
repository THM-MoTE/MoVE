/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.views.shapes

import javafx.scene.paint.Paint
import javafx.scene.shape.{LineTo, MoveTo, Path}


import de.thm.move.types._

import scala.collection.JavaConverters._

class ResizablePath(startPoint: MoveTo, elements:List[LineTo])
  extends Path((startPoint :: elements).asJava)
  with ResizableShape
  with ColorizableShape
  with QuadCurveTransformable
  with PathLike {

  val allElements = startPoint :: elements
  override lazy val edgeCount: Int = allElements.size

  def getPoints:List[Point] = allElements.flatMap {
    case move:MoveTo => List((move.getX, move.getY))
    case line:LineTo => List((line.getX,line.getY))
  }

  override def getFillColor:Paint = null /*Path has no fill => transparent background == null in JavaFx*/
  override def setFillColor(c:Paint):Unit = { /*Path has no fill*/ }
  override def toCurvedShape = QuadCurvePath(this)
  override def copy: ResizablePath = {
    val duplicate = ResizablePath(getPoints)
    duplicate.copyColors(this)
    duplicate.setRotate(getRotate)
    duplicate
  }

  override def resize(idx: Int, delta: (Double, Double)): Unit = {
    val (x,y) = delta
    allElements(idx) match {
      case move:MoveTo =>
        move.setX(move.getX + x)
        move.setY(move.getY + y)
      case line:LineTo =>
        line.setX(line.getX + x)
        line.setY(line.getY + y)
      case _ => //ignore
    }
  }

  override def getEdgePoint(idx: Int): (Double, Double) = getPoints(idx)
}

object ResizablePath {
  def apply(points:List[Point]): ResizablePath = {
    val start = new MoveTo(points.head.x, points.head.y)
    val elements = points.tail.map { case (x,y) => new LineTo(x,y) }
    new ResizablePath(start, elements)
  }

  def apply(curved:QuadCurvePath): ResizablePath = {
    val path = ResizablePath(curved.getUnderlyingPolygonPoints)
    path.copyColors(curved)
    path
  }
}
