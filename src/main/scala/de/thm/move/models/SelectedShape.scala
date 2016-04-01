/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.models

object SelectedShape extends Enumeration {
  type SelectedShape = Value
  val Rectangle, Line, Path, Circle, Polygon = Value
}
