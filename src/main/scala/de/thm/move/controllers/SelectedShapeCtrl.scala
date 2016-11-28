/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.controllers

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color

import de.thm.move.Global._
import de.thm.move.controllers.factorys.ShapeFactory

import de.thm.move.types._
import de.thm.move.views._
import de.thm.move.views.panes.{DrawPanel, SnapLike}
import de.thm.move.views.shapes._

/** Controller for selected shapes. Selected shapes are highlighted by a dotted
 * black border around the bounding-box.
 */
class SelectedShapeCtrl(
    changeLike:ChangeDrawPanelLike,
    grid:SnapLike)
    extends SelectionCtrlLike
    with SelectedTextCtrl
    with SelectedMoveCtrl
    with ColorizeSelectionCtrl {

  val addSelectedShapeProperty = new SimpleBooleanProperty(false)

  private var selectedShapes:List[ResizableShape] = Nil

  override def getSelectedShapes: List[ResizableShape] = selectedShapes
  override def getSnapLike:SnapLike = grid

  private def getSelectionGroups: List[GroupLike] = {
    def findGroups(xs:List[ResizableShape]): List[GroupLike] =
      xs flatMap {
        case g:GroupLike => g :: findGroups(g.childrens)
        case _ => Nil
      }

    findGroups(selectedShapes)
  }

  def setSelectedShape(shape:ResizableShape): Unit = {
    if(addSelectedShapeProperty.get) addToSelectedShapes(shape)
    else replaceSelectedShape(shape)

    shape.getAnchors.foreach(_.setVisible(true))
    shape.rotationAnchors.map(_.getId).foreach(changeLike.removeById)
  }

  private def addSelectionRectangle(shape:ResizableShape): Unit = {
    if(!changeLike.contains(shape.selectionRectangle))
      changeLike.addNode(shape.selectionRectangle)
  }

  private def replaceSelectedShape(shape:ResizableShape): Unit = {
    unselectShapes()
    selectedShapes = List(shape)
    addSelectionRectangle(shape)
  }

  private def addToSelectedShapes(shape:ResizableShape): Unit = {
    //each item only 1 time in the selection
    if(!selectedShapes.contains(shape)) {
      selectedShapes = shape :: selectedShapes
      addSelectionRectangle(shape)
    }
  }

  def rotationMode(): Unit = {
    selectedShapes.foreach { shape =>
      shape.getAnchors.foreach(_.setVisible(false))
      changeLike.addNode(shape.rotationAnchors)
    }
  }

  def unselectShapes(): Unit = {
    for(shape <- selectedShapes) {
      changeLike.remove(shape.selectionRectangle)
    }
    selectedShapes = Nil
  }

  def deleteSelectedShape(): Unit = {
    if(!selectedShapes.isEmpty) {
        val shapeCopy = selectedShapes
        history.execute {
          shapeCopy foreach { shape =>
            changeLike.remove(shape.selectionRectangle)
            shape.rotationAnchors.map(_.getId).foreach(changeLike.removeById)
            changeLike.removeShape(shape)
          }
        } {
          shapeCopy foreach { shape =>
            changeLike.addNode(shape)
            changeLike.addNode(shape.getAnchors:_*)
          }
        }
      selectedShapes = List()
    }
  }

  def groupSelectedElements(): Unit = {
    selectedShapes foreach { x =>
      changeLike.remove(x.selectionRectangle)
    }

    val group = new SelectionGroup(selectedShapes)
    changeLike.addShape(group)

    selectedShapes = List(group)
  }

  def ungroupSelectedElements(): Unit = {
    getSelectionGroups foreach { group =>
      changeLike.remove(group)
      changeLike.remove(group.selectionRectangle)
      group.childrens.foreach { shape =>
        changeLike.addNode(shape)
        changeLike.addNode(shape.getAnchors:_*)
      }
    }
  }

  private def highlightGroupedElements(startBounding:Point,endBounding:Point):Unit = {
    val shapesInBox = changeLike.getElements filter {
      case shape:ResizableShape =>
        //only the elements thar are ResizableShapes and placed inside the bounding
        val shapeBounds = shape.getBoundsInParent
        shapeBounds.getMinX > startBounding.x &&
        shapeBounds.getMaxX < endBounding.x &&
        shapeBounds.getMinY > startBounding.y &&
        shapeBounds.getMaxY < endBounding.y
      case _ => false
    } map(_.asInstanceOf[ResizableShape])

    unselectShapes()
    for(shape <- shapesInBox) {
      changeLike.addNode(shape.selectionRectangle)
    }

    selectedShapes = shapesInBox.toList
  }

  def getGroupSelectionHandler: MouseEvent => Unit = {
    var mouseP = (0.0,0.0)
    //highlight the currently selection-space
    val groupRectangle = ShapeFactory.newRectangle((0,0), 0.0, 0.0)(Color.BLACK,Color.BLACK, 1)
    groupRectangle.getStyleClass.addAll("selection-rectangle")
    groupRectangle.setId(DrawPanel.tmpShapeId)

    def groupHandler(mv:MouseEvent):Unit = mv.getEventType match {
      case MouseEvent.MOUSE_PRESSED =>
        changeLike.addNode(groupRectangle)
        mouseP = (mv.getX,mv.getY)
        groupRectangle.setXY(mouseP)
        groupRectangle.setWidth(0)
        groupRectangle.setHeight(0)
      case MouseEvent.MOUSE_DRAGGED =>
        //adjust selection highlighting
        val w = mv.getX - mouseP.x
        val h = mv.getY - mouseP.y

        groupRectangle.setWidth(w)
        groupRectangle.setHeight(h)
      case MouseEvent.MOUSE_RELEASED =>
        val delta = (mv.getX,mv.getY) - mouseP
        val start = mouseP
        val end = start + delta
        changeLike.remove(groupRectangle)
        highlightGroupedElements(start,end)
      case _ => //ignore other events
    }

    groupHandler
  }
}
