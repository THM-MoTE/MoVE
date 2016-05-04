/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.util

import javafx.scene.paint.{Color, Paint}

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

  implicit object ColorToString extends Convertable[Paint, String] {
    override def convert(p: Paint): String = p match {
    case c:Color => s"""{${(c.getRed*255).toInt},${(c.getGreen*255).toInt},${(c.getBlue*255).toInt}}"""
    case _ => throw new IllegalArgumentException("Can't create rgb-values from non-color paint-values")
    }
  }
}
