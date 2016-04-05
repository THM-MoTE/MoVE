package de.thm.move.views.shapes

import javafx.scene.shape.{QuadCurveTo, MoveTo, Path}
import de.thm.move.models.CommonTypes.Point
import de.thm.move.util.{BindingUtils, GeometryUtils}
import de.thm.move.views.Anchor


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
  val curveTos = for(idx <- 1 until reversedP.size) yield {
    //(startPoint for this becier curve = last added point of the path)
    val (ctrlX, ctrlY) = reversedP(idx) //ctrlPoint := this point
    //endPoint := middleOf(thisPoint, nextPoint)
    val (endX, endY) =
      if(idx+1 < reversedP.size) GeometryUtils.middleOfLine(reversedP(idx), reversedP(idx + 1))
      else GeometryUtils.middleOfLine(reversedP(idx), reversedP.head)

    val curve = new QuadCurveTo(ctrlX,ctrlY, endX,endY)
    curve
  }

  private val (stX,stY) = reversedP.head
  private val (tmpX,tmpY) = GeometryUtils.middleOfLine(reversedP.head, reversedP(1)) //point between head & reversedP(1)
  private val start = new MoveTo(tmpX,tmpY) //shfit path to starting-point
  private val end = new QuadCurveTo(stX,stY, tmpX,tmpY)

  this.getElements.addAll(start)
  this.getElements.addAll(curveTos:_*)
  if(closedShape) {
    this.getElements.addAll(end)
  }

  override val getAnchors: List[Anchor] = for(p <- points) yield {
    new Anchor(p)
  }

  BindingUtils.binAnchorsLayoutToNodeLayout(this)(getAnchors:_*)
  override def setY(y: Double): Unit = setLayoutY(y)
  override def getY: Double = getLayoutY
  override def setX(x: Double): Unit = setLayoutX(x)
  override def getX: Double = getLayoutX

  def toUncurvedShape: ResizableShape
}
