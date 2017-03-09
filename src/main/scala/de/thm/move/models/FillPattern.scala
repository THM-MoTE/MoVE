/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.models

import javafx.scene.canvas._
import javafx.scene.paint._

import de.thm.move.Global._
object FillPattern extends Enumeration {
  type FillPattern = Value
  val None, Solid, HorizontalCylinder, VerticalCylinder,
  Sphere, Horizontal, Vertical, Cross, Forward, Backward, CrossDiag = Value

}
