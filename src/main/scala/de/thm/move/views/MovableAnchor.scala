package de.thm.move.views

import javafx.scene.input.MouseEvent
import javafx.scene.shape.Ellipse

import de.thm.move.controllers.implicits.FxHandlerImplicits._

trait MovableAnchor {
  self: Ellipse =>

  private var deltaX = -1.0
  private var deltaY = -1.0

  self.setOnMousePressed({ me: MouseEvent =>
    deltaX = self.getCenterX - me.getX
    deltaY = self.getCenterY - me.getY
  })

  self.setOnMouseDragged({ me: MouseEvent =>
    self.setCenterX(deltaX + me.getX)
    self.setCenterY(deltaY + me.getY)
  })
}
