/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.models

import java.util.function.Predicate
import javafx.scene.Node

object LinePattern extends Enumeration {
  type LinePattern = Value
  val None, Solid, Dash, Dot, DashDot, DashDotDot = Value

  val linePatternToCssClass: Map[LinePattern.LinePattern, String] =
    Map(
      LinePattern.None -> "none-stroke",
      LinePattern.Solid -> "solid-stroke",
      LinePattern.Dash -> "dash-stroke",
      LinePattern.Dot -> "dotted-stroke",
      LinePattern.DashDot -> "dash-dotted-stroke",
      LinePattern.DashDotDot -> "dash-dotted-dotted-stroke"
    )

  val cssRegex = ".*-stroke"

  //remove old stroke style
  val removeOldCss: (Node) => Unit = { shape =>
    shape.getStyleClass().removeIf(new Predicate[String]() {
      override def test(str:String): Boolean = str.`matches`(cssRegex)
    })
  }
}
