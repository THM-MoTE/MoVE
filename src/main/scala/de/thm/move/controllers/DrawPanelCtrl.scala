/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.controllers

import javafx.scene.Node
import javafx.event.{EventHandler, ActionEvent}
import javafx.scene.input.InputEvent
import scala.collection.JavaConversions._

import de.thm.move.views.shapes.ResizableShape
import de.thm.move.views.DrawPanel

class DrawPanelCtrl(
    val drawPanel: DrawPanel,
    shapeInputHandler:InputEvent => Unit)
    extends ChangeDrawPanelLike {
  private val contextMenuCtrl = new ContextMenuCtrl(drawPanel, this)

    override def addShape(shape: ResizableShape*): Unit = {
      shape foreach { x =>
        x.addEventHandler(InputEvent.ANY, new EventHandler[InputEvent]() {
          override def handle(event: InputEvent): Unit = shapeInputHandler(event)
        })
        contextMenuCtrl.setupContextMenu(x)
        drawPanel.drawShape(x)
      }
    }

    override def addNode(node:Node*): Unit = node foreach drawPanel.drawShape


    /** Removes the given shape with '''it's anchors''' from the DrawPanel */
    override def removeShape(shape: ResizableShape): Unit = {
      drawPanel.remove(shape)
      shape.getAnchors.foreach(drawPanel.remove)
    }

    override def getElements: List[Node] = drawPanel.getChildren.toList
    override def remove(n:Node): Unit = drawPanel.remove(n)
}
