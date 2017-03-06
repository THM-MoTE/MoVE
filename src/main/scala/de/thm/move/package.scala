/**
  * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
  *
  * This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
  */

package de.thm

import javafx.geometry.Point2D

import de.thm.move.views.shapes.{ColorizableShape, PathLike, RectangleLike, ResizableShape}

package object move {
  object types {
    type Point = (Double, Double)
    type RectangularNode = ResizableShape with RectangleLike with ColorizableShape
    type PathNode = ResizableShape with PathLike with ColorizableShape

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
    implicit class PointOps(val p: Point) extends AnyVal {
      @inline
      final def -(that: Point): Point = (p._1 - that._1, p._2 - that._2)

      @inline
      final def /(that: Point): Point = (p._1 / that._1, p._2 / that._2)

      @inline
      final def +(that: Point): Point = (p._1 + that._1, p._2 + that._2)

      @inline
      final def *(factor:Double): Point = map(_*factor)

      @inline
      final def map[A](fn: Double => A): (A, A) = (fn(p._1), fn(p._2))

      @inline
      final def x: Double = p._1

      @inline
      final def y: Double = p._2

      @inline
      final def abs: Point = (Math.abs(p._1), Math.abs(p._2))

      @inline
      final def asJava: Point2D = new Point2D(p._1, p._2)
    }

  }
}
