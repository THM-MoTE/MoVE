package de.thm.move.controllers

import javafx.scene.paint.Color

import de.thm.move.Global
import de.thm.move.Global._
import de.thm.move.views.DrawPanel
import de.thm.move.views.shapes.{ColorizableShape, ResizableShape}
import de.thm.move.views.SelectionGroup
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

  private var selectedShapes:List[ResizableShape] = Nil

  private def coloredSelectedShape: List[ResizableShape with ColorizableShape] =
    selectedShapes flatMap {
      //filter non-colrizable shapes; they have no linepattern
      case colorizable:ColorizableShape => List(colorizable)
    }

  private val linePatternToCssClass: Map[LinePattern.LinePattern, String] =
    LinePattern.linePatternToCssClass

  def setSelectedShape(shape:ResizableShape): Unit = {
    removeSelectedShape
    selectedShapes = List(shape)
    drawPanel.getChildren.add(shape.selectionRectangle)
  }

  def removeSelectedShape: Unit = {
    for(shape <- selectedShapes) {
      drawPanel.remove(shape.selectionRectangle)
    }
    selectedShapes = Nil
  }

  def deleteSelectedShape: Unit = {
    if(!selectedShapes.isEmpty) {
        val shapeCopy = selectedShapes
        history.execute {
          shapeCopy foreach { shape =>
            drawPanel.remove(shape)
            drawPanel.remove(shape.selectionRectangle)
          }
        } {
          shapeCopy foreach { shape =>
            drawPanel.getChildren.add(shape)
            drawPanel.getChildren.addAll(shape.getAnchors:_*)
          }
        }
      selectedShapes = List()
    }
  }

  def setFillColorForSelectedShape(color:Color): Unit = if(!selectedShapes.isEmpty) {
    val shapeAndColor = coloredSelectedShape zip coloredSelectedShape.map(_.getFillColor)
    history.execute {
      for(x <- coloredSelectedShape) {
        x.setFillColor(color)
      }
    } {
      for((x,oldColor) <- shapeAndColor) {
        x.setFillColor(oldColor)
      }
    }
  }

  def setStrokeColorForSelectedShape(color:Color): Unit = if(!selectedShapes.isEmpty) {
    val shapeAndColor = coloredSelectedShape zip coloredSelectedShape.map(_.getStrokeColor)

    history.execute {
      for(x <- coloredSelectedShape)
        x.setStrokeColor(color)
    } {
      for((x,oldColor) <- shapeAndColor)
        x.setStrokeColor(oldColor)
    }
  }

  def setStrokeWidthForSelectedShape(width:Int): Unit = {
    val shapeAndWidth = coloredSelectedShape zip coloredSelectedShape.map(_.getStrokeWidth)

      history.execute {
        for(x <- coloredSelectedShape)
          x.setStrokeWidth(width)
      } {
        for((x,oldWidth) <- shapeAndWidth)
          x.setStrokeWidth(oldWidth)
      }
  }

  def setStrokePattern(linePattern:LinePattern.LinePattern): Unit =
    linePatternToCssClass.get(linePattern) foreach { cssClass =>
      val cssOpt = coloredSelectedShape.
        map(_.getStyleClass().find(_.`matches`(LinePattern.cssRegex)))
      val linePatterns = coloredSelectedShape.map(_.linePattern.get)
      val shapeAndCss = coloredSelectedShape zip (cssOpt zip linePatterns)

      history.execute {
        for(shape <- coloredSelectedShape) {
          LinePattern.removeOldCss(shape)
          shape.getStyleClass().add(cssClass)
          shape.linePattern.set(linePattern)
        }
      } {
        for {
          (shape, (oldCssOpt, oldLinePattern)) <- shapeAndCss
          if oldCssOpt.isDefined
          css = oldCssOpt.get
        } {
            LinePattern.removeOldCss(shape)
            shape.getStyleClass().add(css)
            shape.linePattern.set(oldLinePattern)
          }
      }
    }

  def setFillPattern(fillPattern:FillPattern.FillPattern): Unit = {
    val coloredShapes = coloredSelectedShape map { shape =>
      (shape, shape.oldFillColorProperty.get, shape.getStrokeColor)
    } flatMap {
      case (shape, c1,c2:Color) => List((shape,c1,c2))
      case _ => Nil
    }

    val shapeAndFillPattern = coloredSelectedShape zip (coloredSelectedShape.
      map(_.fillPatternProperty.get) zip coloredSelectedShape.map(_.getFillColor))

    history.execute {
      for( (shape, fillColor, strokeColor) <- coloredShapes ) {
        val newFillColor = FillPattern.getFillColor(fillPattern, fillColor, strokeColor)
        shape.setFillColor(newFillColor)
        shape.fillPatternProperty.set(fillPattern)
      }
    } {
      for( (shape,(oldFillProperty, oldFillGradient)) <- shapeAndFillPattern) {
        shape.setFillColor(oldFillGradient)
        shape.fillPatternProperty.set(oldFillProperty)
      }
    }
  }

  def groupElements(startBounding:Point,endBounding:Point):Unit = {
    val shapesInBox = drawPanel.getChildren() filter {
      case shape:ResizableShape =>
        //only the elements thar are ResizableShapes and placed inside the bounding
        val shapeBounds = shape.getBoundsInLocal
        shapeBounds.getMinX > startBounding.x &&
        shapeBounds.getMaxX < endBounding.x &&
        shapeBounds.getMinY > startBounding.y &&
        shapeBounds.getMaxY < endBounding.y
      case _ => false
    } map(_.asInstanceOf[ResizableShape])

    removeSelectedShape
    /*for(shape <- shapesInBox) {
        drawPanel.getChildren.add(shape.selectionRectangle)
    }
    */

    val selectedGroup = new SelectionGroup(shapesInBox.toList)
    drawPanel.getChildren().add(selectedGroup)
    drawPanel.getChildren().add(selectedGroup.selectionRectangle)

    selectedShapes = shapesInBox.toList
  }

  def getGroupSelectionHandler: MouseEvent => Unit = {
    var mouseP = (0.0,0.0)
    //highlight the currently selection-space
    val groupRectangle = ShapeFactory.newRectangle((0,0), 0.0, 0.0)(Color.BLACK,Color.BLACK, 1)
    groupRectangle.getStyleClass.addAll("selection-rectangle")
    groupRectangle.setVisible(false)
    drawPanel.getChildren.add(groupRectangle)

    def groupHandler(mv:MouseEvent):Unit = mv.getEventType match {
      case MouseEvent.MOUSE_PRESSED =>
        mouseP = (mv.getX,mv.getY)
        groupRectangle.setXY(mouseP)
        groupRectangle.setVisible(true)
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
