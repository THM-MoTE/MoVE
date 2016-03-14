package de.thm.move.controllers.implicits

import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.input.MouseEvent

object FxHandlerImplicits {
  implicit def mouseEventHandler[T >: MouseEvent](fn: T => Unit): EventHandler[MouseEvent] = new EventHandler[MouseEvent]() {
    override def handle(event: MouseEvent): Unit = fn(event)
  }

  implicit def actionEventHandler[T >: ActionEvent](fn: T => Unit): EventHandler[ActionEvent] = new EventHandler[ActionEvent]() {
    override def handle(event: ActionEvent): Unit = fn(event)
  }
}
