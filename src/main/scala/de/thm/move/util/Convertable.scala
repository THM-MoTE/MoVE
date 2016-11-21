/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.util

import javafx.scene.paint.{Color, Paint}

/** Typeclass for convertable values */
trait Convertable[From, To] {
  /** Converts a From into a To */
  def convert(f: From):To
}

/** A marshaller is basically a converter for both directions: encoding and decoding. */
trait Marshaller[From, To] extends Convertable[From, To] {
  override final def convert(f:From): To = encode(f)
  def encode(from:From): To
  def decode(to:To): From
}

/** Converter for String-objects */
trait StringConverter[A] extends Convertable[String,A]
trait StringMarshaller[A] extends Marshaller[String,A]

object Marshaller {
  implicit object StringIntMarshaller extends StringMarshaller[Int] {
    override def encode(from: String): Int = from.toInt
    override def decode(to: Int): String = to.toString
  }
}

object Convertable {
  /** Converts a string into it's color representation */
  implicit object StringToColor extends StringConverter[Color] {
    override def convert(f: String): Color = Color.valueOf(f)
  }

  /** Converts a javafx-color into modelica's representation of colors. */
  implicit object ColorToString extends Convertable[Paint, String] {
    override def convert(p: Paint): String = p match {
    case c:Color => s"""{${(c.getRed*255).toInt},${(c.getGreen*255).toInt},${(c.getBlue*255).toInt}}"""
    case _ => throw new IllegalArgumentException("Can't create rgb-values from non-color paint-values")
    }
  }
}
