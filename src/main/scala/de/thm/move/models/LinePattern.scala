/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.models

object LinePattern extends Enumeration {
  type LinePattern = Value
  val None, Solid, Dash, Dot, DashDot, DashDotDot = Value
}
