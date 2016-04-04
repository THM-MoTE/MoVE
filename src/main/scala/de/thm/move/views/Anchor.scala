/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.views

import javafx.scene.paint.Color
import javafx.scene.shape.{Ellipse, Circle}
import de.thm.move.models.CommonTypes.Point
import de.thm.move.Global

class Anchor(x:Double, y:Double) extends Ellipse(x,y,Anchor.anchorWidth,Anchor.anchorHeight) {
  def this(p:Point) = this(p._1, p._2)

  this.getStyleClass.addAll("anchor")
}

object Anchor {
  val anchorWidth = Global.config.getDouble("anchor.width").getOrElse(2.0)
  val anchorHeight = Global.config.getDouble("anchor.height").getOrElse(2.0)
}
