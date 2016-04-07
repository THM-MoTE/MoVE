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
import scala.collection.JavaConversions._

class ResizablePolygon(val points:List[Double]) extends Polygon(points:_*) with ResizableShape with ColorizableShape with QuadCurveTransformable {

  private val observablePoints = getPoints
  val getAnchors: List[Anchor] =
    (for(i <- 0 until observablePoints.size by 2) yield {
      val xIdx = i
      val yIdx = i+1
      val xProperty = new SimpleDoubleProperty(observablePoints.get(xIdx))
      val yProperty = new SimpleDoubleProperty(observablePoints.get(yIdx))

      xProperty.addListener { (_: Number, newX: Number) =>
        val _ = observablePoints.set(xIdx, newX.doubleValue())
      }
      yProperty.addListener { (_: Number, newX: Number) =>
        val _ = observablePoints.set(yIdx, newX.doubleValue())
      }

      val anchor = new Anchor(xProperty.get(), yProperty.get()) with MovableAnchor
      xProperty.bind(anchor.centerXProperty())
      yProperty.bind(anchor.centerYProperty())
      anchor
    }).toList

  JFxUtils.binAnchorsLayoutToNodeLayout(this)(getAnchors:_*)

  override def getX: Double = getLayoutX()
  override def setY(y: Double): Unit = setLayoutY(y)
  override def getY: Double = getLayoutY()
  override def setX(x: Double): Unit = setLayoutX(x)
  override def toCurvedShape = QuadCurvePolygon(this)
  override def copy: ResizableShape = {
    val duplicate = new ResizablePolygon(getPoints.map(_.doubleValue).toList)
    duplicate.copyColors(this)
    duplicate
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
    polygon.setX(cubed.getX)
    polygon.setY(cubed.getY)
    polygon
  }
}
