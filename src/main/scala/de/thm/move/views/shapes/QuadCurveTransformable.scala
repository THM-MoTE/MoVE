/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views.shapes

trait QuadCurveTransformable extends ResizableShape {
  def toCurvedShape: AbstractQuadCurveShape
}
