package de.thm.move.controllers.implicits

import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.input.{InputEvent, MouseEvent}

object FxHandlerImplicits {
  implicit def mouseEventHandler[T >: MouseEvent](fn: T => Unit): EventHandler[MouseEvent] = new EventHandler[MouseEvent]() {
    override def handle(event: MouseEvent): Unit = fn(event)
  }

  implicit def actionEventHandler[T >: ActionEvent](fn: T => Unit): EventHandler[ActionEvent] = new EventHandler[ActionEvent]() {
    override def handle(event: ActionEvent): Unit = fn(event)
  }

  implicit def changeListener[A](fn: (ObservableValue[_<:A], A, A) => Unit) = new ChangeListener[A] {
    override def changed(observable: ObservableValue[_ <: A], oldValue: A, newValue: A): Unit = fn(observable, oldValue, newValue)
  }
}
