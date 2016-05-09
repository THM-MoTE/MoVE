/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.util

import javafx.event.{Event, EventHandler}

import de.thm.move.controllers.implicits.FxHandlerImplicits._
import javafx.scene.Node
import javafx.scene.control.ChoiceBox

import de.thm.move.views.anchors.Anchor
import de.thm.move.views.shapes.{MovableShape, ResizableShape}

/** General utils for working with JavaFx. */
object JFxUtils {
  /** Adds the given listener to the selectionProperty of the given ChoiceBox.
   * The eventHandler only gets the new value and discards the old value.
   */
  def onChoiceboxChanged[A](box:ChoiceBox[A])(eventHandler: A => Unit): Unit = {
    box.getSelectionModel.
      selectedItemProperty.addListener { (_:A, newA:A) =>
        eventHandler(newA)
      }
  }

  def binAnchorsLayoutToNodeLayout(node:Node)(anchors:Anchor*): Unit = {
    anchors.foreach { anchor =>
      anchor.layoutXProperty().bind(node.layoutXProperty())
      anchor.layoutYProperty().bind(node.layoutYProperty())
    }
  }

  /** Checks if the parent of given Node n is a MovableShape and
    * if it is the given function fn is called with the parent. If the parent
    * isn't a MovableShape the function fn is called with the given Node n.
    */
  def withParentMovableElement[A](n:Node with MovableShape)(fn: MovableShape => A):A =
    (n, n.getParent) match {
      case (_,ms:MovableShape) => fn(ms)
      case (ms:MovableShape,_) => fn(ms)
    }

  def withResizableElement[A](n:Node)(fn: ResizableShape => A):A = (n, n.getParent) match {
    case (_,ms:ResizableShape) => fn(ms)
    case (ms:ResizableShape,_) => fn(ms)
    case _ => throw new IllegalArgumentException(s"that's not a resizableShape: $n")
  }

  /** Creates an EventHandler, calls inside handle() the given function fn and consumes the event afterwards. */
  def withConsumedEvent[A <: Event](fn: A => Unit): EventHandler[A] = new EventHandler[A]() {
    override def handle(event: A): Unit = {
      fn(event)
      event.consume()
    }
  }
}
