/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.util

import de.thm.move.models.CommonTypes.Point
import javafx.geometry.Point2D
import javafx.scene.transform.{Affine, Rotate, Transform}
import PointUtils._
import   scala.math._

object GeometryUtils {

  type Vector2D = Point

  /** Calculates the middle-point M of the line represented by the given 2 points */
  def middleOfLine(start:Point, end:Point): Point = {
    val (startX, startY) = start
    val (endX,endY) = end
    ( (startX+endX)/2, (startY+endY)/2 )
  }

  /** Calculates the middle-point M of the line represented by the given 2 points */
  def middleOfLine(startX:Double, startY:Double, endX:Double, endY:Double): Point =
    middleOfLine((startX,startY), (endX,endY))

  /** Converts the given double into a radius number */
  def asRadius(x:Double):Double = x/2

  /** Returns the closest multiple of x to v */
  def closestMultiple(x:Double, v:Double): Option[Double] = {
    val rem = v / x
    val multiple = BigDecimal(rem).setScale(0, BigDecimal.RoundingMode.HALF_UP).toDouble
    if(v % x == 0) Some(v) //v = multiple of x
    else if(rem % 0.5 == 0) None //remainder is a.5 => no nearest value
    else Some(multiple*x)
  }

  /** Calculates the offset which needs to get added to a (rotated) RectangleLike
    * after a RectangleLike got resized.
    *
    * @note All given Point2D's have to be in untransformed
    *       coordinates.
    *       This function is an adaption of Christopher Schölzel's equivalent in Java, Processing.
    * @param cOld the middle-point of the rectangle BEFORE the resize
    * @param cNew the middle-point of the rectangle AFTER the resize
    * @param deg the rotation-degree
    * @param isoCorner the corner which shouldn't change;
    *                  in general the opposite corner of the resized corner
    */
  def calculateRotationOffset(cOld: Point2D, cNew: Point2D, deg: Double, isoCorner: Point2D): Point2D =  {
    val r = new Rotate(deg, cOld.getX(), cOld.getY());
    val rotCorner = r.transform(isoCorner);
    val relCorner = isoCorner.subtract(cNew);
    // find new center for new shape
    val c = Math.cos(Math.toRadians(deg));
    val s = Math.sin(Math.toRadians(deg));
    val cNewTrans = new Point2D(
            rotCorner.getX() - c * relCorner.getX() + s * relCorner.getY(),
            rotCorner.getY() - c * relCorner.getY() - s * relCorner.getX()
    );
    // find delta vector between old and new center
    val delta = cNewTrans.subtract(cNew);
    delta;
  }

  /** Calculates the middle-point between the given 4 points
    * if the 4 points represent a rectangular-bounds.
    */
  def rectangleMiddlePoint(p1:Point, p2:Point, p3:Point, p4:Point): Point = {
    List(p1,p2,p3,p4).foldLeft((0.0,0.0)) {
      case (acc, elem) => acc + elem
    } / (4.0,4.0)
  }

  /* Calculates the vector between the given 2 points */
  def vectorOf(startPoint:Point, endPoint:Point): Vector2D =
    endPoint - startPoint

  /* Calculates the magnitude of the given vector */
  def vectorMagnitude(v:Vector2D): Double =
    sqrt(pow(v.x, 2) + pow(v.y, 2))

  /* Calculates the scalar-product from the given 2 vectors */
  def scalar(v1:Vector2D, v2:Vector2D): Double =
    v1.x * v2.x + v1.y * v2.y
}
