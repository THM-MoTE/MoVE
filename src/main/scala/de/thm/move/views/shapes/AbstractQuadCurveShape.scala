package de.thm.move.views.shapes

import javafx.scene.shape.{QuadCurveTo, MoveTo, LineTo, Path, PathElement}
import de.thm.move.models.CommonTypes.Point
import de.thm.move.util.{BindingUtils, GeometryUtils}
import de.thm.move.util.PointUtils._
import de.thm.move.views.Anchor
import de.thm.move.views.MovableAnchor
import de.thm.move.controllers.implicits.FxHandlerImplicits._

abstract class AbstractQuadCurveShape(val points:List[Point], closedShape:Boolean) extends Path with ResizableShape with ColorizableShape {

  /**Implementation nodes:
    * The given points represent the normal polygon/path!
    * - Each point represent a control-point of the becier curve
    * - the start and end points of the becier curve is the middle point
    * of 2 given points!
    *
    * Example:
    * You have Points p1,p2,p3
    * a becier curve between this lines is defined as follows:
    * startPoint of becier curve = middlePointOf(p1,p2)
    * endPoint of becier curve = middlePointOf(p2,p3)
    * controlPoint of becier curve = p2
    */

  val reversedP = points.reverse
  val curves = adjustPath(reversedP.toArray)
  this.getElements.addAll(curves:_*)

  private def adjustPath(points:Array[Point]): List[PathElement] = {
    val (stX,stY) = points.head
    val (tmpX,tmpY) = GeometryUtils.middleOfLine(points.head, points(1)) //point between head & points(1)
    val start = new MoveTo(tmpX,tmpY) //shfit path to starting-point
    val end = new QuadCurveTo(stX,stY, tmpX,tmpY)

    val xs = start::(for(idx <- 1 until points.size) yield {
      //(startPoint for this becier curve = last added point of the path)
      val (ctrlX, ctrlY) = points(idx) //ctrlPoint := this point
      //endPoint := middleOf(thisPoint, nextPoint)
      val (endX, endY) =
        if(idx+1 < points.size) GeometryUtils.middleOfLine(points(idx), points(idx + 1))
        else GeometryUtils.middleOfLine(points(idx), points.head)

      val curve = new QuadCurveTo(ctrlX,ctrlY, endX,endY)
      curve
    }).toList

    if(closedShape) xs ::: List(end)
    else xs
  }

  private def getPathXY:PartialFunction[PathElement, Point] = {
    case move:MoveTo => (move.getX,move.getY)
    case line:LineTo => (line.getX,line.getY)
    case cubic:QuadCurveTo => (cubic.getX,cubic.getY)
  }

  private def setPathXY(elem:PathElement, ctrlP:Point) = {
    elem match {
      case cubic:QuadCurveTo =>
        cubic.setControlX(ctrlP.x)
        cubic.setControlY(ctrlP.y)
      case move:MoveTo =>
        move.setX(ctrlP.x)
        move.setY(ctrlP.y)
      case _ => throw new IllegalStateException("Can'T set XY for non cubicCurveTo values")
    }
  }

  private val underlyingPolygonPoints = reversedP.toArray

  override val getAnchors: List[Anchor] =
    (for(
      idx <- 0 until underlyingPolygonPoints.size;
      ctrlP = underlyingPolygonPoints(idx)
    ) yield {
      val anchor = new Anchor(ctrlP) with MovableAnchor

      anchor.centerXProperty.addListener { (_:Number, newV:Number) =>
        underlyingPolygonPoints(idx) = (newV.doubleValue,underlyingPolygonPoints(idx).y)
        this.getElements.clear()
        this.getElements.addAll(adjustPath(underlyingPolygonPoints):_*)
        ()
      }
      anchor.centerYProperty.addListener{ (_:Number, newV:Number) =>
        underlyingPolygonPoints(idx) = (underlyingPolygonPoints(idx).x, newV.doubleValue)
        this.getElements.clear()
        this.getElements.addAll(adjustPath(underlyingPolygonPoints):_*)
        ()
      }
      anchor
    }).toList

  BindingUtils.binAnchorsLayoutToNodeLayout(this)(getAnchors:_*)
  override def setY(y: Double): Unit = setLayoutY(y)
  override def getY: Double = getLayoutY
  override def setX(x: Double): Unit = setLayoutX(x)
  override def getX: Double = getLayoutX

  def toUncurvedShape: ResizableShape
}
