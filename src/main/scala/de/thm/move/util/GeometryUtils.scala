package de.thm.move.util

import de.thm.move.models.CommonTypes.Point

object GeometryUtils {

  /**Calculates the middle-point M of the line represented by the given 2 points*/
  def middleOfLine(start:Point, end:Point): Point = {
    val (startX, startY) = start
    val (endX,endY) = end
    ( (startX+endX)/2, (startY+endY)/2 )
  }

  /**Calculates the middle-point M of the line represented by the given 2 points*/
  def middleOfLine(startX:Double, startY:Double, endX:Double, endY:Double): Point =
    middleOfLine((startX,startY), (endX,endY))

  /**Converts the given double into a radius number*/
  def asRadius(x:Double):Double = x/2
}
