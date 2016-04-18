package de.thm.move.util

import javafx.scene.paint.Color

trait Convertable[From, To] {
  def convert(f: From):To
}

trait StringConverter[A] extends Convertable[String,A]

object Convertable {
  implicit object StringToColor extends StringConverter[Color] {
    override def convert(f: String): Color = Color.valueOf(f)
  }
}