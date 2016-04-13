package de.thm.move.controllers

import javafx.scene.paint.Color

import de.thm.move.Global
import de.thm.move.Global._
import de.thm.move.views.DrawPanel
import de.thm.move.views.shapes.{ColorizableShape, ResizableShape}
import de.thm.move.models.LinePattern
import de.thm.move.models.FillPattern
import de.thm.move.models.CommonTypes._
import de.thm.move.util.PointUtils._
import java.util.function.Predicate
import javafx.scene.Node
import javafx.scene.paint._
import javafx.scene.input.MouseEvent

import scala.collection.JavaConversions._
import de.thm.move.controllers.factorys.ShapeFactory

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

  def groupElements(startBounding:Point,endBounding:Point):Unit = {
    println("sb: "+startBounding)
    println("eb: "+endBounding)
    val shapesInBox = drawPanel.getChildren().filter {
      case shape:ResizableShape =>
        val shapeBounds = shape.getBoundsInLocal
        println(shape)
        println("shBounds: "+shapeBounds)
        shapeBounds.getMinX > startBounding.x &&
        shapeBounds.getMaxX < endBounding.x &&
        shapeBounds.getMinY > startBounding.y &&
        shapeBounds.getMaxY < endBounding.y
      case _ => false
    }
  }

  def getGroupSelectionHandler: MouseEvent => Unit = {
    var mouseP = (0.0,0.0)
    //highlight the currently selection-space
    var groupRectangle = ShapeFactory.newRectangle((0,0), 0.0, 0.0)(Color.BLACK,Color.BLACK, 1)
    groupRectangle.getStyleClass.addAll("selection-rectangle")
    groupRectangle.setVisible(false)
    drawPanel.getChildren.add(groupRectangle)

    def groupHandler(mv:MouseEvent):Unit = mv.getEventType match {
      case MouseEvent.MOUSE_PRESSED =>
        mouseP = (mv.getX,mv.getY)
        groupRectangle.setVisible(true)
        groupRectangle.setXY(mouseP)
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
        groupRectangle.setVisible(false)
        groupElements(start,end)
      case _ => //ignore other events
    }

    groupHandler
  }
}
