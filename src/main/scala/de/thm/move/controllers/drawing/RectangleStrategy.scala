package de.thm.move.controllers.drawing

import de.thm.move.controllers.ChangeDrawPanelLike
import de.thm.move.views.shapes._

class RectangleStrategy(changeLike:ChangeDrawPanelLike) extends RectangularStrategy(changeLike, new ResizableRectangle((0,0), 0,0)) {

}
