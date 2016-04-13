package de.thm.move.controllers.implicits

import scala.language.implicitConversions

object ConcurrentImplicits {
  implicit def fnRunnable[A](fn:  => A): Runnable = new Runnable() {
    override def run(): Unit = fn
  }
}
