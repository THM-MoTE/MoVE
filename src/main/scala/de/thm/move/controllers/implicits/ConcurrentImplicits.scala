package de.thm.move.controllers.implicits

object ConcurrentImplicits {
  implicit def fnRunnable[A](fn:  => A): Runnable = new Runnable() {
    override def run(): Unit = fn
  }
}
