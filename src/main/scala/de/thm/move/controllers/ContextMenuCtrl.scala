/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.controllers

import java.io.ByteArrayInputStream
import java.nio.file.{Files, Paths}
import javafx.event.ActionEvent
import javafx.scene.Node
import javafx.scene.control.{MenuItem, SeparatorMenuItem, TextField}
import javafx.scene.image.Image
import javafx.scene.input.{MouseButton, MouseEvent}

import de.thm.move.Global._
import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.views.ShapeContextMenu
import de.thm.move.views.panes.DrawPanel
import de.thm.move.views.shapes._

/** Controller for context-menus of shapes */
class ContextMenuCtrl(drawPanel:DrawPanel, changeLike:ChangeDrawPanelLike) {

  private def newContextMenu(underlyingElement:ResizableShape):ShapeContextMenu = {
    val menu = new ShapeContextMenu
    menu.inBackgroundItem.setOnAction { ae:ActionEvent => onBackgroundPressed(ae, underlyingElement) }
    menu.inForegroundItem.setOnAction { ae:ActionEvent => onForegroundPressed (ae, underlyingElement) }
    menu.duplicateElementItem.setOnAction { ae:ActionEvent => onDuplicateElementPressed(ae, underlyingElement) }
    menu.resetRotationElementItem.setOnAction { ae:ActionEvent => onResetRotationElementPressed(ae, underlyingElement) }

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
      case txt:ResizableText =>
        val editItem = new MenuItem("Edit text")
        editItem.setOnAction { ae: ActionEvent => onEditText(ae, txt) }
        menu.getItems.add(editItem)
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

  def onResetRotationElementPressed(ae:ActionEvent, shape:ResizableShape): Unit = {
    val oldRotate = shape.getRotate
    history.execute {
      shape.setRotate(0)
    } {
      shape.setRotate(oldRotate)
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

  def onEditText(ae:ActionEvent, txt:ResizableText): Unit = {
    val txtField = new TextField(txt.getText)
    txtField.setLayoutX(txt.getX)
    txtField.setLayoutY(txt.getY)
    txtField.setRotate(txt.getRotate)
    txtField.setOnAction { _:ActionEvent =>
      txt.setText(txtField.getText)
      changeLike.addShape(txt)
      changeLike.remove(txtField)
    }
    changeLike.removeShape(txt)
    changeLike.addNode(txtField)
  }
}
