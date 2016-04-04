package de.thm.move.controllers

import javafx.event.ActionEvent
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.input.MouseButton
import javafx.scene.control.{SeparatorMenuItem, Separator, MenuItem}
import javafx.scene.input.{InputEvent, MouseEvent}
import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.views.shapes.{QuadCurvePolygon, ResizablePolygon}
import de.thm.move.views.{DrawPanel, ShapeContextMenu}
import de.thm.move.Global._

class ContextMenuCtrl(drawPanel:DrawPanel) {

  private def newContextMenu(underlyingElement:Node):ShapeContextMenu = {
    val menu = new ShapeContextMenu
    menu.inBackgroundItem.setOnAction { ae:ActionEvent => onBackgroundPressed(ae, underlyingElement) }
    menu.inForegroundItem.setOnAction { ae:ActionEvent => onForegroundPressed (ae, underlyingElement) }

    underlyingElement match {
      case polygon:ResizablePolygon =>
        val becierItem = new MenuItem("Smooth")
        becierItem.setOnAction{ ae:ActionEvent => onBecierPressed(ae, polygon) }
        menu.getItems.addAll(new SeparatorMenuItem(), becierItem)
      case _ => //ignore
    }

    menu
  }

  def setupContextMenu(node:Node): Unit = {
    val contextMenu = newContextMenu(node)
    node.setOnMouseClicked { me: MouseEvent =>
      if (me.getButton == MouseButton.SECONDARY) {
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

  private def onBecierPressed(ae:ActionEvent, polygon:ResizablePolygon): Unit = {
    //TODO add listener from polygon onto curved polygon
    val curvedPolygon = QuadCurvePolygon(polygon)
    curvedPolygon.setX(polygon.getX)
    curvedPolygon.setY(polygon.getY)

    println("poly anchorcnt "+polygon.getAnchors.size)
    println("curved anchorcnt "+curvedPolygon.getAnchors.size)

    drawPanel.getChildren.remove(polygon)
    drawPanel.getChildren.removeAll(polygon.getAnchors:_*)
    drawPanel.getChildren.add(curvedPolygon)
    drawPanel.getChildren.addAll(curvedPolygon.getAnchors:_*)
  }
}
