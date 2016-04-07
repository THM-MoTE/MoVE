package de.thm.move.controllers

import javafx.scene.paint.Color

import de.thm.move.Global
import de.thm.move.Global._
import de.thm.move.views.DrawPanel
import de.thm.move.views.shapes.{ColorizableShape, ResizableShape}
import de.thm.move.models.LinePattern
import de.thm.move.models.FillPattern
import java.util.function.Predicate
import javafx.scene.paint.Paint
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.Stop

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
    Map(
      LinePattern.Solid -> "solid-stroke",
      LinePattern.Dash -> "dash-stroke",
      LinePattern.Dot -> "dotted-stroke",
      LinePattern.DashDot -> "dash-dotted-stroke",
      LinePattern.DashDotDot -> "dash-dotted-dotted-stroke"
      )

  private def getFillColor(fillPattern:FillPattern.FillPattern, fillC:Color, strokeC:Color):Paint = {
    /**
      * Implementation Nodes:
      * LinearGradients - Horizontal:
      *   create gradient from (bottom to middle); reflect them on
      *    (middle to top)
      * LinearGradients - Vertical:
      *   create gradient from (left to middle); reflect them on
      *    (middle to right)
      */
    val stops = List(
      new Stop(0,strokeC), //start with strokeColor
      //create a gentle transition to fillColor
      new Stop(0.45,fillC),
      new Stop(0.55,fillC),
      new Stop(1, strokeC) //end with strokeColor
      )
    fillPattern match {
      case FillPattern.HorizontalCylinder =>
        new LinearGradient(0,0,0,1,true,CycleMethod.REFLECT, stops:_*)
      case FillPattern.VerticalCylinder =>
        new LinearGradient(0,0,1,0,true,CycleMethod.REFLECT, stops:_*)
      case _ => println("WARNING: not implemented yet!"); null
    }
  }

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
      //remove old stroke style
      shape.getStyleClass().removeIf(new Predicate[String]() {
          override def test(str:String): Boolean = str.`matches`(".*-stroke")
        })
      shape.getStyleClass().add(cssClass)
      shape.setLinePattern(linePattern)
    }
  def setFillPattern(fillPattern:FillPattern.FillPattern): Unit =
    coloredSelectedShape map { shape =>
      (shape, shape.getFillColor, shape.getStrokeColor)
    } flatMap {
      case (shape, c1:Color,c2:Color) => Some((shape,c1,c2))
      case _ => None
      } foreach { case (shape, fillColor, strokeColor) =>
      println("set fill: "+fillPattern)
      val newFillColor = getFillColor(fillPattern, fillColor, strokeColor)
      shape.setFillColor(newFillColor)
    }
}
