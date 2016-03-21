package de.thm.move.views

import javafx.scene.input.MouseEvent
import javafx.scene.shape.Ellipse

import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.history.History
import de.thm.move.history.History.Command
import de.thm.move.Global._

trait MovableAnchor {
  self: Ellipse =>

  private var deltaX = -1.0
  private var deltaY = -1.0

  //undo-/redo command
  private var command: (=> Unit) => Command = x => { History.emptyAction }

  self.setOnMousePressed { me: MouseEvent =>
    val oldX = self.getCenterX
    val oldY = self.getCenterY

    deltaX = oldX - me.getX
    deltaY = oldY - me.getY

    command = History.partialAction{
      self.setCenterX(oldX)
      self.setCenterY(oldY)
    }
  }

  self.setOnMouseDragged { me: MouseEvent =>
    history.execute(command {
      self.setCenterX(deltaX + me.getX)
      self.setCenterY(deltaY + me.getY)
    })

  }
}
