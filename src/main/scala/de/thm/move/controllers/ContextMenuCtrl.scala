package de.thm.move.controllers

import javafx.event.ActionEvent
import javafx.scene.Node
import javafx.scene.input.MouseEvent

import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.views.{DrawPanel, ShapeContextMenu}
import de.thm.move.Global._

class ContextMenuCtrl(drawPanel:DrawPanel) {

  private def newContextMenu(underlyingElement:Node):ShapeContextMenu = {
    val menu = new ShapeContextMenu
    menu.inBackgroundItem.setOnAction { ae:ActionEvent => onBackgroundPressed(ae, underlyingElement) }
    menu.inForegroundItem.setOnAction { ae:ActionEvent => onForegroundPressed (ae, underlyingElement) }
    menu
  }

  def setupContextMenu(node:Node): Unit = {
    val contextMenu = newContextMenu(node)
    node.setOnMousePressed { me: MouseEvent =>
      if (me.isSecondaryButtonDown) {
        contextMenu.show(node, me.getScreenX, me.getScreenY)
      }
    }
  }

  private def onForegroundPressed(ae:ActionEvent, underlyingElement:Node): Unit = {
    val oldIdx = drawPanel.getChildren.indexOf(underlyingElement)
    history.execute {
      drawPanel.getChildren.remove(underlyingElement)
      drawPanel.getChildren.add(underlyingElement)
      ()
    } {
      drawPanel.getChildren.remove(underlyingElement)
      drawPanel.getChildren.add(oldIdx, underlyingElement)
    }
  }

  private def onBackgroundPressed(ae:ActionEvent, underlyingElement:Node): Unit = {
    val oldIdx = drawPanel.getChildren.indexOf(underlyingElement)
    history.execute {
      drawPanel.getChildren.remove(underlyingElement)
      drawPanel.getChildren.add(0, underlyingElement)
      ()
    } {
      drawPanel.getChildren.remove(underlyingElement)
      drawPanel.getChildren.add(oldIdx, underlyingElement)
    }
  }
}
