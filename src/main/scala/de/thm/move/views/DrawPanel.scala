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
import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.models.CommonTypes._
import javafx.beans.binding.Bindings

import de.thm.move.views.shapes._

class DrawPanel() extends Pane {
  private var shapes = List[Node]()

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
}
