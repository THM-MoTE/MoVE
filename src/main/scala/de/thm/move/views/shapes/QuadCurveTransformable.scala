/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views.shapes

/** A bezier-curved shape that can get transformed into a non-curved/normal shape */
trait QuadCurveTransformable extends ResizableShape {
  def toCurvedShape: AbstractQuadCurveShape
}
