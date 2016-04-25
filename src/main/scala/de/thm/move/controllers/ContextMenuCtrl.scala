/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.controllers

import java.io.ByteArrayInputStream
import java.nio.file.{Files, Paths}
import javafx.event.ActionEvent
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.scene.input.MouseButton
import javafx.scene.control.{SeparatorMenuItem, Separator, MenuItem}
import javafx.scene.input.{InputEvent, MouseEvent}
import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.views.shapes._
import de.thm.move.views.{DrawPanel, ShapeContextMenu}
import de.thm.move.Global._

/** Controller for context-menus of shapes */
class ContextMenuCtrl(drawPanel:DrawPanel, changeLike:ChangeDrawPanelLike) {

  private def newContextMenu(underlyingElement:ResizableShape):ShapeContextMenu = {
    val menu = new ShapeContextMenu
    menu.inBackgroundItem.setOnAction { ae:ActionEvent => onBackgroundPressed(ae, underlyingElement) }
    menu.inForegroundItem.setOnAction { ae:ActionEvent => onForegroundPressed (ae, underlyingElement) }
    menu.duplicateElementItem.setOnAction { ae:ActionEvent => onDuplicateElementPressed(ae, underlyingElement) }

    underlyingElement match {
      case polygon:QuadCurveTransformable =>
        val becierItem = new MenuItem("Smooth")
        becierItem.setOnAction{ ae:ActionEvent => onBecierPressed(ae, polygon) }
        menu.getItems.addAll(new SeparatorMenuItem(), becierItem)
      case curved:AbstractQuadCurveShape =>
        val polygonItem = new MenuItem("Unsmooth")
        polygonItem.setOnAction { ae:ActionEvent => onUnsmoothPressed(ae, curved) }
        menu.getItems.addAll(new SeparatorMenuItem(), polygonItem)
      case img:ResizableImage if img.srcEither.isLeft =>
        val encodeBase64Item = new MenuItem("Encode as Base64")
        encodeBase64Item.setOnAction { ae:ActionEvent => onEncodePressed(ae, img)}
        menu.getItems.add(encodeBase64Item)
      case _ => //ignore
    }

    menu
  }

  /** Sets up a new context-menu for the given node */
  def setupContextMenu(node:ResizableShape): Unit = {
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
    history.execute {
      changeLike.removeShape(polygon)
      changeLike.addShape(curvedShape)
      changeLike.addNode(curvedShape.getAnchors)
    } {
      changeLike.removeShape(curvedShape)
      changeLike.addShape(polygon)
      changeLike.addNode(polygon.getAnchors)
    }
  }

  private def onUnsmoothPressed(ae:ActionEvent, curved:AbstractQuadCurveShape): Unit = {
    val uncurvedShape = curved.toUncurvedShape
    history.execute {
      changeLike.removeShape(curved)
      changeLike.addShape(uncurvedShape)
      changeLike.addNode(uncurvedShape.getAnchors)
    } {
      changeLike.removeShape(uncurvedShape)
      changeLike.addShape(curved)
      changeLike.addNode(curved.getAnchors)
    }
  }

  def onDuplicateElementPressed(ae:ActionEvent, shape:ResizableShape): Unit = {
    val duplicate = shape.copy
    history.execute {
      changeLike.addShape(duplicate)
      changeLike.addNode(duplicate.getAnchors)
    } {
      changeLike.removeShape(duplicate)
    }
  }

  def onEncodePressed(ae:ActionEvent, resImg:ResizableImage): Unit = resImg.srcEither match {
      case Left(uri) =>
        val bytes = Files.readAllBytes(Paths.get(uri))
        val img = new Image(new ByteArrayInputStream(bytes))
        val newImg = ResizableImage(bytes, img)
        newImg.copyPosition(resImg)
        changeLike.remove(resImg)
        changeLike.addShapeWithAnchors(newImg)
      case _ => //ignore
  }
}
