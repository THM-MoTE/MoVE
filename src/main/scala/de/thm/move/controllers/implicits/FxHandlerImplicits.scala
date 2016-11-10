/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.controllers.implicits

import java.util.function.{Predicate, Function => JavaFunction}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.input.{KeyEvent, MouseEvent}

import scala.language.implicitConversions

object FxHandlerImplicits {
  implicit def mouseEventHandler[T >: MouseEvent](fn: T => Unit): EventHandler[MouseEvent] = new EventHandler[MouseEvent]() {
    override def handle(event: MouseEvent): Unit = fn(event)
  }

  implicit def keyEventHandler[T >: KeyEvent](fn: T => Unit): EventHandler[KeyEvent] = new EventHandler[KeyEvent]() {
    override def handle(ke:KeyEvent): Unit = fn(ke)
  }

  implicit def actionEventHandler[T >: ActionEvent](fn: T => Unit): EventHandler[ActionEvent] = new EventHandler[ActionEvent]() {
    override def handle(event: ActionEvent): Unit = fn(event)
  }

  implicit def changeListener[A](fn: (A, A) => Unit):ChangeListener[A] = new ChangeListener[A] {
    override def changed(observable: ObservableValue[_ <: A], oldValue: A, newValue: A): Unit = fn(oldValue, newValue)
  }

  implicit def changeListener[A](fn: (ObservableValue[_<:A], A, A) => Unit):ChangeListener[A] = new ChangeListener[A] {
    override def changed(observable: ObservableValue[_<: A], oldValue: A, newValue: A): Unit = fn(observable, oldValue, newValue)
  }

  implicit def eventHandler[E <: javafx.event.Event](fn: E => Unit):EventHandler[E] = new EventHandler[E] {
    override def handle(event: E): Unit = fn(event)
  }

  implicit def predicate[A](fn: A => Boolean):Predicate[A] = new Predicate[A]() {
    override def test(a:A): Boolean = fn(a)
  }

  implicit def function[A, B](fn: A => B): JavaFunction[A,B] = new JavaFunction[A,B]() {
    override def apply(v: A): B = fn(v)
  }
}
