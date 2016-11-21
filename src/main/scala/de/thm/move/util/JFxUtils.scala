/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.util

import javafx.beans.property.ObjectProperty
import javafx.event.{Event, EventHandler}
import javafx.scene.Node
import javafx.scene.control.ChoiceBox
import javafx.scene.input.{KeyEvent, KeyCode}

import de.thm.move.implicits.FxHandlerImplicits._
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

  /** Copies the value of property2 into property1 */
  def copyProperty[A](property1:ObjectProperty[A], property2:ObjectProperty[A]): Unit =
    property1.set(property2.get)

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

  /** Creates a new EventHandler[A] in which fn get's executed if predicate returns true. */
  def filteredEventHandler[A <: Event](predicate: A => Boolean)(fn: => Unit): EventHandler[A] =
    new EventHandler[A]() {
      override def handle(a:A):Unit = if(predicate(a)) fn
    }

    /** A predicate for filtering a KeyEvent-Stream by the pressed KeyCode. */
    val byKeyCode: KeyCode => KeyEvent => Boolean = code => kv => kv.getCode == code
}
