/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.util

import de.thm.move.models.CommonTypes.Point
import javafx.geometry.Point2D
import javafx.scene.transform.{Affine, Rotate, Transform}

import scala.math.BigDecimal

object GeometryUtils {

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
}
