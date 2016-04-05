package de.thm.move.views.shapes

trait QuadCurveTransformable extends ResizableShape {
  def toCurvedShape: AbstractQuadCurveShape
}
