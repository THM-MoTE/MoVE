package de.thm.move.views

import de.thm.move.models.CommonTypes._

/** Represents elements that can calculate a distance for a snapping-mechanism to a specific element.
  *
  * E.G.: A grid with lines on which elements can get snapped (a.k.a. snap-to-grid).
  * */
trait SnapLike {
  /** Returns the x-coordinate for the closest element to deltaX. */
  def getClosestXPosition(deltaX:Double): Option[Int]
  /** Returns the y-coordinate for the closest element to deltaY. */
  def getClosestYPosition(deltaY:Double): Option[Int]

  /** Returns the point for the closest element to p. */
  def getClosestPosition(p:Point):Option[Point] = {
    val xOpt = getClosestXPosition(p._1)
    val yOpt = getClosestYPosition(p._2)
    for {
      x <- xOpt
      y <- yOpt
    } yield (x.toDouble,y.toDouble)
  }
}
