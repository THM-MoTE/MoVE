/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views.shapes

import javafx.scene.shape.{QuadCurveTo, MoveTo, LineTo, Path, PathElement}
import de.thm.move.models.CommonTypes.Point
import de.thm.move.util.{JFxUtils, GeometryUtils}
import de.thm.move.util.PointUtils._
import de.thm.move.views.Anchor
import de.thm.move.views.MovableAnchor
import de.thm.move.controllers.implicits.FxHandlerImplicits._

/** A shape with quadratic-bezier-curved edge-points.
  *
  * See Modelica Spec 3.1 - Ch. 18.6.1.2 page 226, 227
  */
abstract class AbstractQuadCurveShape(
  points: List[Point],
  closedShape: Boolean)
  extends Path
  with ResizableShape
  with ColorizableShape {

  /**
   * Implementation nodes:
   * The given points represent the normal polygon/path!
   * - Each point represent a control-point of the becier curve
   * - the start and end points of the becier curve is the middle point
   * of 2 given points!
   *
   * Example:
   * You have Points p1,p2,p3
   * a becier curve between this lines is defined as follows:
   * startPoint of becier curve = middlePointOf(p1,p2)
   * endPoint of becier curve = middlePointOf(p2,p3)
   * controlPoint of becier curve = p2
   */

  val reversedP = points.reverse
  /*the path behind this element; the points gets adjusted whenever someone
   *resizes this element
   */
  private val underlyingPolygonPoints = reversedP.toArray

  val curves = adjustPath(reversedP.toArray)
  this.getElements.addAll(curves: _*)

  private def adjustPath(points: Array[Point]): List[PathElement] = {
    val (stX, stY) = points.head
    val (tmpX, tmpY) = GeometryUtils.middleOfLine(points.head, points(1)) //point between head & points(1)
    /**
     * shfit path to starting-point if this is a closedShape (e.g. polygon)
     * if not draw a line between original startpoint and starting-point
     * for first bezier curve
     */
    val start =
      if (closedShape) List(new MoveTo(tmpX, tmpY))
      else List(new MoveTo(stX, stY), new LineTo(tmpX, tmpY))
    val end = new QuadCurveTo(stX, stY, tmpX, tmpY)

    val xs = start ::: (for (idx <- 1 until points.size) yield {
      //(startPoint for this becier curve = last added point of the path)
      val (ctrlX, ctrlY) = points(idx) //ctrlPoint := this point
      //endPoint := middleOf(thisPoint, nextPoint)
      val (endX, endY) =
        if (idx + 1 < points.size) GeometryUtils.middleOfLine(points(idx), points(idx + 1))
        else GeometryUtils.middleOfLine(points(idx), points.head)

      new QuadCurveTo(ctrlX, ctrlY, endX, endY)
    }).toList

    /**
     * close the path if it's a closedShape (e.g. polygon)
     * if not draw a line between last beziercurve end-point & endpoint of
     * original path
     */
    if (closedShape) xs ::: List(end)
    else xs.init ::: List(new LineTo(points.last.x, points.last.y))
  }

  def getUnderlyingPolygonPoints: List[Point] = underlyingPolygonPoints.toList

  override val getAnchors: List[Anchor with MovableAnchor] =
    (for (
      idx <- 0 until underlyingPolygonPoints.size;
      ctrlP = underlyingPolygonPoints(idx)
    ) yield {
      val anchor = new Anchor(ctrlP) with MovableAnchor
      /* Everytime this shape get's resized the path get's replaced
       * by a new path
       */
      anchor.centerXProperty.addListener { (_: Number, newV: Number) =>
        underlyingPolygonPoints(idx) = (newV.doubleValue, underlyingPolygonPoints(idx).y)
        this.getElements.clear()
        this.getElements.addAll(adjustPath(underlyingPolygonPoints): _*)
        ()
      }
      anchor.centerYProperty.addListener { (_: Number, newV: Number) =>
        underlyingPolygonPoints(idx) = (underlyingPolygonPoints(idx).x, newV.doubleValue)
        this.getElements.clear()
        this.getElements.addAll(adjustPath(underlyingPolygonPoints): _*)
        ()
      }
      anchor
    }).toList

  JFxUtils.binAnchorsLayoutToNodeLayout(this)(getAnchors: _*)
  override def move(delta:Point):Unit = getAnchors.foreach(_.move(delta))

  def toUncurvedShape: ResizableShape
}
