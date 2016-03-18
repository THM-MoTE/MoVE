package de.thm.move.views.shapes

import de.thm.move.views.Anchor

trait ResizableShape {
  def getAnchors: List[Anchor]
}
