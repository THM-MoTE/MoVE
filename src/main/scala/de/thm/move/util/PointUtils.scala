package de.thm.move.util

import de.thm.move.models.CommonTypes._

object PointUtils {
  implicit class PointOps(val p:Point) extends AnyVal {
    def -(that:Point):Point = (p._1-that._1, p._2-that._2)
    def +(that:Point):Point = (p._1+that._1, p._2+that._2)
    def map[A](fn: Double => A):(A,A) = (fn(p._1), fn(p._2))
  }
}
