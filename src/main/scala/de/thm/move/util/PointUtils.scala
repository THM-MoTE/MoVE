/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.util

import de.thm.move.models.CommonTypes._

/** Utils for working with the type [[de.thm.move.models.CommonTypes.Point]]
  * (This object contains a implicit value-class which makes calculation on a point possible.)
  */
object PointUtils {
  /** A valueclass for extending the type Point.
    * This class makes it possible to write:
    * {{{
    *   val point1 = (5,20)
    *   val point2 = (1,1)
    *   val sum = point1 + point2
    * }}}
    * see [[http://docs.scala-lang.org/overviews/core/value-classes.html Scala Documentation]]
    * for more infos about value-classes.
    */
  implicit class PointOps(val p:Point) extends AnyVal {
    @inline
    final def -(that:Point):Point = (p._1-that._1, p._2-that._2)
    @inline
    final def +(that:Point):Point = (p._1+that._1, p._2+that._2)
    @inline
    final def map[A](fn: Double => A):(A,A) = (fn(p._1), fn(p._2))
    @inline
    final def x:Double = p._1
    @inline
    final def y:Double = p._2
    @inline
    final def abs:Point = (Math.abs(p._1), Math.abs(p._2))
  }
}
