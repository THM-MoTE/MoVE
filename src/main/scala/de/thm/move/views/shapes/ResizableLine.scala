/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views.shapes

import javafx.scene.input.MouseEvent
import javafx.scene.shape.Line

import de.thm.move.Global._
import de.thm.move.history.History
import de.thm.move.history.History.Command
import de.thm.move.models.CommonTypes.Point
import de.thm.move.util.JFxUtils._
import de.thm.move.util.PointUtils._
import de.thm.move.views.{MovableAnchor, Anchor}
import de.thm.move.controllers.implicits.FxHandlerImplicits._

class ResizableLine(
         start:Point,
         end:Point,
         strokeSize:Int)
   extends Line(start._1, start._2, end._1, end._2)
   with ResizableShape
   with ColorizableShape {
  setStrokeWidth(strokeSize)

  val startAnchor = new Anchor(start)
  val endAnchor = new Anchor(end)
  val getAnchors: List[Anchor] = List(startAnchor, endAnchor)

  private def pointChanged():Unit = {
    val startPoint2d = localToParent(getStartX, getStartY)
    val endPoint2d = localToParent(getEndX, getEndY)
    startAnchor.setCenterX(startPoint2d.getX)
    startAnchor.setCenterY(startPoint2d.getY)
    endAnchor.setCenterX(endPoint2d.getX)
    endAnchor.setCenterY(endPoint2d.getY)
  }

  startXProperty().addListener { (_:Number,_:Number) =>
    pointChanged()
  }
  startYProperty().addListener { (_:Number,_:Number) =>
    pointChanged()
  }
  endXProperty().addListener { (_:Number,_:Number) =>
    pointChanged()
  }
  endYProperty().addListener { (_:Number,_:Number) =>
    pointChanged()
  }

  //element got rotated; adjust anchors
  rotateProperty().addListener { (_:Number, _:Number) =>
    pointChanged()
  }

  //undo-/redo command
  private var command: (=> Unit) => Command = x => { History.emptyAction }

  startAnchor.setOnMousePressed(withConsumedEvent { _:MouseEvent =>
    val (oldX,oldY) = (getStartX, getStartY)
    command = History.partialAction {
      setStartX(oldX)
      setStartY(oldY)
    }
  })

  startAnchor.setOnMouseDragged(withConsumedEvent { me:MouseEvent =>
      setStartX(me.getX)
      setStartY(me.getY)
  })

  startAnchor.setOnMouseReleased(withConsumedEvent { _:MouseEvent =>
    val (oldX,oldY) = (getStartX,getStartY)
    history.save(command {
      setStartX(oldX)
      setStartY(oldY)
    })
  })

  endAnchor.setOnMousePressed(withConsumedEvent { _:MouseEvent =>
    val (oldX,oldY) = (getEndX, getEndY)
    command = History.partialAction {
      setEndX(oldX)
      setEndY(oldY)
    }
  })

  endAnchor.setOnMouseDragged(withConsumedEvent { me:MouseEvent =>
      setEndX(me.getX)
      setEndY(me.getY)
  })

  endAnchor.setOnMouseReleased(withConsumedEvent { _:MouseEvent =>
    val (oldX,oldY) = (getEndX, getEndY)
    history.save(command {
      setEndX(oldX)
      setEndY(oldY)
    })
  })

  binAnchorsLayoutToNodeLayout(this)(getAnchors:_*)

  override def move(delta:Point):Unit = {
    val (x,y) = delta
    setStartX(getStartX + x)
    setStartY(getStartY + y)
    setEndX(getEndX + x)
    setEndY(getEndY + y)
  }

  override def copy: ResizableShape = {
    val duplicate = new ResizableLine(
      (getStartX,getStartY),
      (getEndX,getEndY),
      strokeSize)
    duplicate.copyColors(this)
    duplicate.setRotate(getRotate)
    duplicate
  }
}
