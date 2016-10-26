/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.controllers.implicits

import java.util.function._
import scala.language.implicitConversions

object LambdaImplicits {
  implicit def function[A,B](f: A => B): Function[A,B] = new Function[A,B] {
    override def apply(a:A): B = f(a)
  }

  implicit def supplier[A](f: => A): Supplier[A] = new Supplier[A] {
    override def get(): A = f
  }

  implicit def consumer[A](f: A => Unit): Consumer[A] = new Consumer[A] {
    override def accept(a:A): Unit = f(a)
  }
  implicit def consumer[A](f:  => Unit): Consumer[A] = new Consumer[A] {
    override def accept(a:A): Unit = f
  }
}
