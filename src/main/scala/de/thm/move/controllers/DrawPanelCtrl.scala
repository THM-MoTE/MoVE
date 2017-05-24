/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.controllers

import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.input.InputEvent

import de.thm.move.views.panes.DrawPanel
import de.thm.move.views.shapes.ResizableShape

import scala.collection.JavaConverters._

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

    override def getElements: List[Node] = drawPanel.getChildren.asScala.toList
    override def remove(n:Node): Unit = drawPanel.remove(n)
}
