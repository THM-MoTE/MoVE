/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views.shapes

import javafx.scene.Node
import de.thm.move.views.Anchor
import de.thm.move.util.PointUtils._
import de.thm.move.models.CommonTypes._

trait ResizableShape extends Node with MovableShape {

  val selectionRectangle = new SelectionRectangle(this)

  val getAnchors: List[Anchor]
  /** Creates a '''exact copy''' of this element. */
  def copy: ResizableShape
}
