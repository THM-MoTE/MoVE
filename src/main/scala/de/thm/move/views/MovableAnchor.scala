/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


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

  self.setOnMouseReleased { _: MouseEvent =>
    val x = self.getCenterX
    val y = self.getCenterY
    history.save(command {
      self.setCenterX(x)
      self.setCenterY(y)
    })
  }

  self.setOnMouseDragged { me: MouseEvent =>
    self.setCenterX(deltaX + me.getX)
    self.setCenterY(deltaY + me.getY)
  }
}
