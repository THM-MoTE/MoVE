package de.thm.move.controllers

import javafx.event.ActionEvent
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.input.MouseButton
import javafx.scene.control.{SeparatorMenuItem, Separator, MenuItem}
import javafx.scene.input.{InputEvent, MouseEvent}
import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.views.shapes.{QuadCurveTransformable, QuadCurvePolygon, ResizablePolygon, AbstractQuadCurveShape}
import de.thm.move.views.{DrawPanel, ShapeContextMenu}
import de.thm.move.Global._

/** Controller for context-menus of shapes */
class ContextMenuCtrl(drawPanel:DrawPanel, changeLike:ChangeDrawPanelLike) {

  private def newContextMenu(underlyingElement:Node):ShapeContextMenu = {
    val menu = new ShapeContextMenu
    menu.inBackgroundItem.setOnAction { ae:ActionEvent => onBackgroundPressed(ae, underlyingElement) }
    menu.inForegroundItem.setOnAction { ae:ActionEvent => onForegroundPressed (ae, underlyingElement) }

    underlyingElement match {
      case polygon:QuadCurveTransformable =>
        val becierItem = new MenuItem("Smooth")
        becierItem.setOnAction{ ae:ActionEvent => onBecierPressed(ae, polygon) }
        menu.getItems.addAll(new SeparatorMenuItem(), becierItem)
      case curved:AbstractQuadCurveShape =>
        val polygonItem = new MenuItem("Unsmooth")
        polygonItem.setOnAction { ae:ActionEvent => onUnsmoothPressed(ae, curved) }
        menu.getItems.addAll(new SeparatorMenuItem(), polygonItem)
      case _ => //ignore
    }

    menu
  }

  /** Sets up a new context-menu for the given node */
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

  private def onBecierPressed(ae:ActionEvent, polygon:QuadCurveTransformable): Unit = {
    val curvedShape = polygon.toCurvedShape
    changeLike.removeShape(polygon)
    changeLike.addShape(curvedShape)
    changeLike.addShape(curvedShape.getAnchors:_*)
  }

  private def onUnsmoothPressed(ae:ActionEvent, curved:AbstractQuadCurveShape): Unit = {
    val uncurvedShape = curved.toUncurvedShape
    changeLike.removeShape(curved)
    changeLike.addShape(uncurvedShape)
    changeLike.addShape(uncurvedShape.getAnchors:_*)
  }
}