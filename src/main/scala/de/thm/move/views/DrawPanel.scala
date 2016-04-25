/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.value.{ObservableValue}
import javafx.event.{EventHandler}
import javafx.scene.input.KeyEvent;
import javafx.geometry.Bounds
import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape._
import javafx.scene.image.{ImageView, Image}
import javafx.scene.input.{InputEvent}
import scala.collection.JavaConverters._
import de.thm.move.views.shapes._
import de.thm.move.models.CommonTypes._

class DrawPanel() extends Pane {
  private var shapes = List[Node]()

  getStyleClass().add("draw-pane")

  def drawShape[T <: Node](n:T):Unit = {
    super.getChildren.add(n)

    shapes = n :: shapes
  }

  def remove[A <: Node](shape:A): Unit = {
    shapes = shapes.filterNot(_ == shape)
    shape match {
      case rs:ResizableShape =>
        getChildren.removeAll(rs.getAnchors:_*)
        getChildren.remove(shape)
      case _ => getChildren.remove(shape)
    }
  }

  def removeWhile(pred: Node => Boolean): Unit = {
    val removingShapes = shapes.takeWhile(pred)
    shapes = shapes.dropWhile(pred)

    getChildren.removeAll(removingShapes:_*)
  }


  def removeWhileIdx(pred: (Node, Int) => Boolean): Unit = {
    val shapeWithidx = shapes.zipWithIndex
    val removingShapes = shapeWithidx.takeWhile {
      case (n, idx) => pred(n,idx)
    }.map(_._1)

    shapes = shapeWithidx.dropWhile  {
      case (n, idx) => pred(n,idx)
    }.map(_._1)

    getChildren.removeAll(removingShapes:_*)
  }

  def setSize(p:Point): Unit = {
    val (x,y) = p
    setPrefSize(x, y)
    setMinSize(x, y)
    setMaxSize(x, y)
  }

  def setSize(w:Double,h:Double):Unit = setSize((w,h))

  def getShapes: List[Node] = getChildren.asScala.filterNot { x =>
    x.isInstanceOf[Anchor] ||
    x.isInstanceOf[SelectionGroup] ||
    x.isInstanceOf[SelectionRectangle]
  }.toList
}
