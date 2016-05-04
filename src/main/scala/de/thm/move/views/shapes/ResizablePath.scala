/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views.shapes

import javafx.scene.paint.Paint
import javafx.scene.shape.{LineTo, MoveTo, Path}

import de.thm.move.util.JFxUtils

import collection.JavaConversions._
import de.thm.move.models.CommonTypes.Point
import de.thm.move.views.anchors.Anchor
import de.thm.move.util.PointUtils._
import de.thm.move.views.anchors.Anchor

class ResizablePath(startPoint: MoveTo, elements:List[LineTo])
  extends Path(startPoint :: elements)
  with ResizableShape
  with ColorizableShape
  with QuadCurveTransformable
  with PathLike {

  val allElements = startPoint :: elements
  override val edgeCount: Int = allElements.size
  val getAnchors: List[Anchor] = genAnchors

  def getPoints:List[Point] = allElements.flatMap {
    case move:MoveTo => List((move.getX, move.getY))
    case line:LineTo => List((line.getX,line.getY))
  }

  override def getFillColor:Paint = null /*Path has no fill => transparent background == null in JavaFx*/
  override def setFillColor(c:Paint):Unit = { /*Path has no fill*/ }
  override def toCurvedShape = QuadCurvePath(this)
  override def copy: ResizableShape = {
    val duplicate = ResizablePath(getPoints)
    duplicate.copyColors(this)
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
