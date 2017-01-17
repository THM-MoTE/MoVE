/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.views.anchors

import javafx.scene.shape.Ellipse

import de.thm.move.types._
import de.thm.move.Global


class Anchor(x:Double, y:Double)
  extends Ellipse(x,y,Anchor.anchorWidth,Anchor.anchorHeight) {
  def this(p:Point) = this(p._1, p._2)

  this.getStyleClass.addAll("anchor")
}

object Anchor {
  val anchorWidth = Global.config.getDouble("anchor.width").getOrElse(2.0)
  val anchorHeight = Global.config.getDouble("anchor.height").getOrElse(2.0)
}
