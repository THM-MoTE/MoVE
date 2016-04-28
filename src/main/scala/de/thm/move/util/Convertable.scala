/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.util

import javafx.scene.paint.Color

/** Typeclass for convertable values */
trait Convertable[From, To] {
  /** Converts a From into a To */
  def convert(f: From):To
}

/** Converter for String-objects */
trait StringConverter[A] extends Convertable[String,A]

object Convertable {
  /** Converts a string into it's color representation */
  implicit object StringToColor extends StringConverter[Color] {
    override def convert(f: String): Color = Color.valueOf(f)
  }
}
