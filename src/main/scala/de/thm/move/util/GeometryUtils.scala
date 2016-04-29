/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.util

import scala.math.BigDecimal
import de.thm.move.models.CommonTypes.Point

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
}
