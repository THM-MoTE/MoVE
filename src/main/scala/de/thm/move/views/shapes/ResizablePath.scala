package de.thm.move.views.shapes

import javafx.scene.paint.Paint
import javafx.scene.shape.{MoveTo, LineTo, Path}
import de.thm.move.util.JFxUtils

import collection.JavaConversions._

import de.thm.move.models.CommonTypes.Point
import de.thm.move.views.{MovableAnchor, Anchor}
import de.thm.move.util.PointUtils._

class ResizablePath(startPoint: MoveTo, elements:List[LineTo]) extends Path(startPoint :: elements) with ResizableShape with ColorizableShape with QuadCurveTransformable {

  val allElements = startPoint :: elements

  override val getAnchors: List[Anchor] =
    allElements.flatMap {
      case move:MoveTo =>
        val anchor = new Anchor(move.getX,move.getY) with MovableAnchor
        move.xProperty.bind(anchor.centerXProperty)
        move.yProperty.bind(anchor.centerYProperty)
        List(anchor)
      case line:LineTo =>
        val anchor = new Anchor(line.getX,line.getY) with MovableAnchor
        line.xProperty.bind(anchor.centerXProperty)
        line.yProperty.bind(anchor.centerYProperty)

        List(anchor)
    }

  JFxUtils.binAnchorsLayoutToNodeLayout(this)(getAnchors:_*)

  def getPoints:List[Point] = allElements.flatMap {
    case move:MoveTo => List((move.getX, move.getY))
    case line:LineTo => List((line.getX,line.getY))
  }
  override def setY(y: Double): Unit = setLayoutY(y)
  override def getY: Double = getLayoutY
  override def setX(x: Double): Unit = setLayoutX(x)
  override def getX: Double = getLayoutX
  override def getFillColor:Paint = null /*Path has no fill*/
  override def setFillColor(c:Paint):Unit = { /*Path has no fill*/ }
  override def toCurvedShape = QuadCurvePath(this)
  override def copy: ResizableShape = {
    val duplicate = ResizablePath(getPoints)
    duplicate.copyColors(this)
    duplicate
  }
}

object ResizablePath {
  def apply(points:List[Point]): ResizablePath = {
    val start = new MoveTo(points.head.x, points.head.y)
    val elements = points.tail.map { case (x,y) => new LineTo(x,y) }
    new ResizablePath(start, elements)
  }

  def apply(curved:QuadCurvePath): ResizablePath = {
    val path = ResizablePath(curved.getUnderlyingPolygonPoints)
    path.copyColors(curved)
    path.setX(curved.getX)
    path.setY(curved.getY)
    path
  }
}
