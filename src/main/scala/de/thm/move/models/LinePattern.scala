/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.models

import java.util.function.Predicate
import javafx.scene.Node
import javafx.scene.shape.Shape

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
