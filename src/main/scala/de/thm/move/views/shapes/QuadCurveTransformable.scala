/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views.shapes

/** A non-curved/normal shape that can get transformed into a curved shape */
trait QuadCurveTransformable extends ResizableShape {
  /** Returns a version with bezier-curves of this shape */
  def toCurvedShape: AbstractQuadCurveShape
}
