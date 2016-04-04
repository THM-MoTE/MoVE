package de.thm.move.controllers

import javafx.scene.paint.Color

import de.thm.move.Global
import de.thm.move.Global._
import de.thm.move.views.DrawPanel
import de.thm.move.views.shapes.{ColorizableShape, ResizableShape}

/** Controller for selected shapes. Selected shapes are highlighted by a dotted
 * black border around the bounding-box.
 */
class SelectedShapeCtrl(drawPanel:DrawPanel) {

  private var selectedShape:Option[ResizableShape] = None

  def setSelectedShape(shape:ResizableShape): Unit = {
    selectedShape match {
      case Some(oldShape) =>
        drawPanel.getChildren.remove(oldShape.selectionRectangle)
        drawPanel.getChildren.add(shape.selectionRectangle)
        selectedShape = Some(shape)
      case _ =>
        selectedShape = Some(shape)
        drawPanel.getChildren.add(shape.selectionRectangle)
    }
  }

  def removeSelectedShape: Unit = {
    selectedShape foreach { shape =>
      drawPanel.remove(shape.selectionRectangle)
      selectedShape = None
    }
  }

  def deleteSelectedShape: Unit = {
    selectedShape foreach { shape =>
      Global.history.execute {
        drawPanel.remove(shape)
        drawPanel.remove(shape.selectionRectangle)
        selectedShape = None
      } {
        drawPanel.getChildren.add(shape)
        drawPanel.getChildren.addAll(shape.getAnchors:_*)
      }
    }
  }

  def setFillColorForSelectedShape(color:Color): Unit = {
    selectedShape flatMap {
      case x:ColorizableShape => Some(x)
      case _ => None
    } foreach { x =>
      val oldColor = x.getFillColor
      history.execute(x.setFillColor(color))(x.setFillColor(oldColor))
    }
  }

  def setStrokeColorForSelectedShape(color:Color): Unit = {
    selectedShape flatMap {
      case x:ColorizableShape => Some(x)
      case _ => None
    } foreach { x =>
      val oldColor = x.getStrokeColor
      history.execute(x.setStrokeColor(color))(x.setStrokeColor(oldColor))
    }
  }

  def setStrokeWidthForSelectedShape(width:Int): Unit = {
    selectedShape flatMap {
      case x:ColorizableShape => Some(x)
      case _ => None
    } foreach { x =>
      val oldWidth = x.getStrokeWidth
      history.execute(x.setStrokeWidth(width))(x.setStrokeWidth(oldWidth))
    }
  }
}
