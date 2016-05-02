/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views.shapes

import javafx.scene.input.MouseEvent
import javafx.scene.paint.Paint
import javafx.scene.shape.{MoveTo, LineTo, Path}
import javafx.geometry.Point3D
import de.thm.move.history.History
import de.thm.move.history.History.Command
import de.thm.move.util.JFxUtils

import collection.JavaConversions._

import de.thm.move.util.JFxUtils._
import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.models.CommonTypes.Point
import de.thm.move.views.{MovableAnchor, Anchor}
import de.thm.move.util.GeometryUtils
import de.thm.move.util.PointUtils._
import de.thm.move.Global._

class ResizablePath(startPoint: MoveTo, elements:List[LineTo])
  extends Path(startPoint :: elements)
  with ResizableShape
  with ColorizableShape
  with QuadCurveTransformable {

  val allElements = startPoint :: elements

  private def moveToChanged(move:MoveTo, anchor:Anchor): Unit = {
    val point2d = localToParent(move.getX,move.getY)
    anchor.setCenterX(point2d.getX)
    anchor.setCenterY(point2d.getY)
  }

  private def lineToChanged(line:LineTo, anchor:Anchor): Unit = {
    val point2d = localToParent(line.getX,line.getY)
    anchor.setCenterX(point2d.getX)
    anchor.setCenterY(point2d.getY)
  }

  override val getAnchors: List[Anchor] =
    allElements.flatMap {
      case move:MoveTo =>
        val anchor = new Anchor(move.getX,move.getY)
        rotateProperty().addListener { (_:Number,_:Number) =>
          moveToChanged(move, anchor)
        }
        move.xProperty.addListener { (_:Number,_:Number) =>
          moveToChanged(move, anchor)
        }
        move.yProperty.addListener { (_:Number,_:Number) =>
          moveToChanged(move, anchor)
        }
        var command: (=> Unit) => Command = x => { History.emptyAction }
          //save for un-/redo
        anchor.setOnMousePressed(withConsumedEvent { _:MouseEvent =>
          val x = move.getX
          val y = move.getY
          command = History.partialAction {
            move.setX(x)
            move.setY(y)
          }
        })
        anchor.setOnMouseDragged(withConsumedEvent { me:MouseEvent =>
          move.setX(me.getX)
          move.setY(me.getY)
        })
        anchor.setOnMouseReleased(withConsumedEvent { me:MouseEvent =>
          val x = me.getX
          val y = me.getY
          history.save(command {
            move.setX(x)
            move.setY(y)
          })
        })
        List(anchor)
      case line:LineTo =>
        val anchor = new Anchor(line.getX,line.getY)
        rotateProperty().addListener { (_:Number,newV:Number) =>
          lineToChanged(line, anchor)
        }
        line.xProperty.addListener { (_:Number,_:Number) =>
          lineToChanged(line, anchor)
        }
        line.yProperty.addListener { (_:Number,_:Number) =>
          lineToChanged(line, anchor)
        }
        var command: (=> Unit) => Command = x => { History.emptyAction }
        //save for un-/redo
        anchor.setOnMousePressed(withConsumedEvent { _:MouseEvent =>
          val x = line.getX
          val y = line.getY
          command = History.partialAction {
            line.setX(x)
            line.setY(y)
          }
        })
        anchor.setOnMouseDragged(withConsumedEvent { me:MouseEvent =>
          line.setX(me.getX)
          line.setY(me.getY)
        })
        anchor.setOnMouseReleased(withConsumedEvent { me:MouseEvent =>
          val x = me.getX
          val y = me.getY
          history.save(command {
            line.setX(x)
            line.setY(y)
          })
        })

        List(anchor)
    }

  JFxUtils.binAnchorsLayoutToNodeLayout(this)(getAnchors:_*)

  def getPoints:List[Point] = allElements.flatMap {
    case move:MoveTo => List((move.getX, move.getY))
    case line:LineTo => List((line.getX,line.getY))
  }
  override def move(delta:Point):Unit = {
    allElements.foreach {
      case move:MoveTo =>
        move.setX(move.getX+delta.x)
        move.setY(move.getY+delta.y)
      case line:LineTo =>
        line.setX(line.getX+delta.x)
        line.setY(line.getY+delta.y)
    }
  }
  override def getFillColor:Paint = null /*Path has no fill*/
  override def setFillColor(c:Paint):Unit = { /*Path has no fill*/ }
  override def toCurvedShape = QuadCurvePath(this)
  override def copy: ResizableShape = {
    val duplicate = ResizablePath(getPoints)
    duplicate.copyColors(this)
    duplicate.setRotate(getRotate)
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
    path
  }
}
