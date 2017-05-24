/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.controllers

import java.net.URI
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.ActionEvent
import javafx.scene.control.TextField
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.text.Font

import de.thm.move.controllers.drawing._
import de.thm.move.controllers.factorys.ShapeFactory
import de.thm.move.implicits.FxHandlerImplicits._
import de.thm.move.models.SelectedShape
import de.thm.move.models.SelectedShape._
import de.thm.move.util.GeometryUtils
import de.thm.move.types._
import de.thm.move.views.anchors.Anchor
import de.thm.move.views.panes.DrawPanel
import de.thm.move.views.shapes._

/** Controller for drawing new shapes or adding existing shapes to the drawPanel. */
class DrawCtrl(changeLike:ChangeDrawPanelLike) {

  private val drawStrategies =
      Map(SelectedShape.Rectangle -> new RectangleStrategy(changeLike),
        SelectedShape.Circle -> new CircleStrategy(changeLike),
        SelectedShape.Line -> new LineStrategy(changeLike),
        SelectedShape.Path -> new PathStrategy(changeLike),
        SelectedShape.Polygon -> new PolygonStrategy(changeLike)
      )

  private val tmpShapeId = DrawPanel.tmpShapeId

  def getDrawHandler: (SelectedShape, MouseEvent) => (Color, Color, Int) => Unit = {
    def drawHandler(shape:SelectedShape, mouseEvent:MouseEvent)(fillColor:Color, strokeColor:Color, selectedThickness:Int): Unit = {
      drawStrategies.get(shape).foreach { strategy =>
        strategy.setColor(fillColor, strokeColor, selectedThickness)
        strategy.dispatchEvent(mouseEvent)
      }
    }

    drawHandler
  }

  /**Removes all temporary shapes (identified by temporaryId) from the given node.*/
  private def removeTmpShapes(temporaryId:String): Unit = {
    val removingNodes = changeLike.getElements.filter {
      n => n.getId == temporaryId
    }
    removingNodes foreach changeLike.remove
  }

  /** Aborts a running drawing-process */
  def abortDrawingProcess(): Unit = {
    removeTmpShapes(tmpShapeId)
    drawStrategies.values.foreach(_.reset())
  }

  def drawImage(imgUri:URI): Unit = {
    val imgview = ShapeFactory.newImage(imgUri)
    changeLike.addShape(imgview)
    changeLike.addNode(imgview.getAnchors)
  }

  def drawText(x:Double,y:Double,color:Color,font:Font): Unit = {
    val text = new TextField()
    text.setId(tmpShapeId)
    text.setOnAction { _:ActionEvent =>
      //replace TextField with ResizableText
      changeLike.remove(text)
      val txt = new ResizableText(text.getText, x,y, font)
      txt.setFontColor(color)
      changeLike.addShape(txt)
    }
    text.setLayoutX(x)
    text.setLayoutY(y)
    changeLike.addNode(text)
    text.requestFocus()
  }
}
