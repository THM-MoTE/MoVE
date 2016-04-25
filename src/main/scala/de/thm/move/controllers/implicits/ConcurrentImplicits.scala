/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.controllers.implicits

import scala.language.implicitConversions

object ConcurrentImplicits {
  implicit def fnRunnable[A](fn:  => A): Runnable = new Runnable() {
    override def run(): Unit = fn
  }
}
