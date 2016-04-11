package de.thm.move.controllers

import javafx.scene.paint.Color

import de.thm.move.Global
import de.thm.move.Global._
import de.thm.move.views.DrawPanel
import de.thm.move.views.shapes.{ColorizableShape, ResizableShape}
import de.thm.move.models.LinePattern
import de.thm.move.models.FillPattern
import java.util.function.Predicate
import javafx.scene.paint._

import scala.collection.JavaConversions._

/** Controller for selected shapes. Selected shapes are highlighted by a dotted
 * black border around the bounding-box.
 */
class SelectedShapeCtrl(drawPanel:DrawPanel) {

  private var selectedShape:Option[ResizableShape] = None

  private def coloredSelectedShape: Option[ResizableShape with ColorizableShape] =
    selectedShape.flatMap {
      //filter non-colrizable shapes; they have no linepattern
      case colorizable:ColorizableShape => Some(colorizable)
    }

  private val linePatternToCssClass: Map[LinePattern.LinePattern, String] =
    LinePattern.linePatternToCssClass

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

  def setStrokePattern(linePattern:LinePattern.LinePattern): Unit =
    coloredSelectedShape.zip(linePatternToCssClass.get(linePattern)) foreach {
      case (shape, cssClass) =>
      val oldCss = shape.getStyleClass().find(_.`matches`(LinePattern.cssRegex))
      val oldLinePattern = shape.linePattern.get

      history.execute {
        shape.getStyleClass().add(cssClass)
        shape.linePattern.set(linePattern)
      } {
        oldCss foreach { css =>
          LinePattern.removeOldCss(shape)
          shape.getStyleClass().add(css)
          shape.linePattern.set(oldLinePattern)
        }
      }
    }

  def setFillPattern(fillPattern:FillPattern.FillPattern): Unit =
    coloredSelectedShape map { shape =>
      (shape, shape.oldFillColorProperty.get, shape.getStrokeColor)
    } flatMap {
      case (shape, c1,c2:Color) => Some((shape,c1,c2))
      case _ => None
      } foreach { case (shape, fillColor, strokeColor) =>
      val newFillColor = FillPattern.getFillColor(fillPattern, fillColor, strokeColor)
      val oldFillProperty = shape.fillPatternProperty.get
      val oldFillGradient = shape.getFillColor
      history.execute {
        shape.setFillColor(newFillColor)
        shape.fillPatternProperty.set(fillPattern)
      } {
        shape.setFillColor(oldFillGradient)
        shape.fillPatternProperty.set(oldFillProperty)
      }
    }
}
