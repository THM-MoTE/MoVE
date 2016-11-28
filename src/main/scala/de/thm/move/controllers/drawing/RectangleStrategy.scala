package de.thm.move.controllers.drawing

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Paint

import de.thm.move.controllers.ChangeDrawPanelLike
import de.thm.move.views.shapes._
import de.thm.move.types._

class RectangleStrategy(changeLike:ChangeDrawPanelLike) extends RectangularStrategy(changeLike, new ResizableRectangle((0,0), 0,0)) {

}
