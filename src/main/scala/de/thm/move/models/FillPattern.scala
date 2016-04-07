/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.models

object FillPattern extends Enumeration {
  type FillPattern = Value
  val None, Solid, HorizontalCylinder, VerticalCylinder, Sphere = Value
}
