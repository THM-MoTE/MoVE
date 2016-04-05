package de.thm.move.views.shapes

import javafx.scene.shape.{QuadCurveTo, MoveTo, LineTo}
import de.thm.move.models.CommonTypes.Point
import javafx.scene.paint.Paint

class QuadCurvePath(points:List[Point]) extends AbstractQuadCurveShape(points, false) {
  override def getFillColor:Paint = null /*Path has no fill*/
  override def setFillColor(c:Paint):Unit = { /*Path has no fill*/ }

  override def toUncurvedShape: ResizableShape = ResizablePath(this)
}

object QuadCurvePath {
  def apply(rePath:ResizablePath):QuadCurvePath = {
    val points:List[Point] = rePath.getPoints
    val path = new QuadCurvePath(points)
    path.copyColors(rePath)
    path.setX(rePath.getX)
    path.setY(rePath.getY)
    path
  }
}
