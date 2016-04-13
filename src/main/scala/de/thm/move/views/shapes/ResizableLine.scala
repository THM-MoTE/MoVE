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
import de.thm.move.util.JFxUtils
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

  startAnchor.centerXProperty().bind(startXProperty())
  startAnchor.centerYProperty().bind(startYProperty())
  endAnchor.centerXProperty().bind(endXProperty())
  endAnchor.centerYProperty().bind(endYProperty())

  //undo-/redo command
  private var command: (=> Unit) => Command = x => { History.emptyAction }

  startAnchor.setOnMousePressed { _:MouseEvent =>
    val (oldX,oldY) = (getStartX, getStartY)
    command = History.partialAction {
      setStartX(oldX)
      setStartY(oldY)
    }
  }

  startAnchor.setOnMouseDragged { me:MouseEvent =>
      setStartX(me.getX)
      setStartY(me.getY)
  }

  startAnchor.setOnMouseReleased { _:MouseEvent =>
    val (oldX,oldY) = (getStartX,getStartY)
    history.save(command {
      setStartX(oldX)
      setStartY(oldY)
    })
  }

  endAnchor.setOnMousePressed { _:MouseEvent =>
    val (oldX,oldY) = (getEndX, getEndY)
    command = History.partialAction {
      setEndX(oldX)
      setEndY(oldY)
    }
  }

  endAnchor.setOnMouseDragged { me:MouseEvent =>
      setEndX(me.getX)
      setEndY(me.getY)
  }

  endAnchor.setOnMouseReleased { _:MouseEvent =>
    val (oldX,oldY) = (getEndX, getEndY)
    history.save(command {
      setEndX(oldX)
      setEndY(oldY)
    })
  }

  JFxUtils.binAnchorsLayoutToNodeLayout(this)(getAnchors:_*)

  override def getX: Double = getLayoutX
  override def setY(y: Double): Unit = setLayoutY(y)
  override def getY: Double = getLayoutY
  override def setX(x: Double): Unit = setLayoutX(x)
  override def copy: ResizableShape = {
    val duplicate = new ResizableLine(
      (getStartX,getStartY),
      (getEndX,getEndY),
      strokeSize)
    duplicate.copyColors(this)
    duplicate
  }
}
