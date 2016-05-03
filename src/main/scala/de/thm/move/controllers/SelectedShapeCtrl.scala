/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.controllers

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment

import de.thm.move.Global
import de.thm.move.Global._
import de.thm.move.controllers.factorys.ShapeFactory
import de.thm.move.history.History
import de.thm.move.models.CommonTypes._
import de.thm.move.models.{FillPattern, LinePattern}
import de.thm.move.util.JFxUtils._
import de.thm.move.util.PointUtils._
import de.thm.move.views.shapes._
import de.thm.move.views._

import scala.collection.JavaConversions._

/** Controller for selected shapes. Selected shapes are highlighted by a dotted
 * black border around the bounding-box.
 */
class SelectedShapeCtrl(
    changeLike:ChangeDrawPanelLike,
    grid:SnapLike) {

  val addSelectedShapeProperty = new SimpleBooleanProperty(false)

  private var selectedShapes:List[ResizableShape] = Nil

  /** Gets all shapes that are colorizable and removes groups if they exist */
  private def coloredSelectedShape: List[ResizableShape with ColorizableShape] = {
    def findColorizables(xs:List[ResizableShape]): List[ResizableShape with ColorizableShape] =
      xs flatMap {
        //filter non-colrizable shapes; they have no linepattern
        case colorizable:ColorizableShape => List(colorizable)
        case g:GroupLike => findColorizables(g.childrens)
        case _ => Nil
      }

    findColorizables(selectedShapes)
  }

  private def getSelectionGroups: List[GroupLike] = {
    def findGroups(xs:List[ResizableShape]): List[GroupLike] =
      xs flatMap {
        case g:GroupLike => g :: findGroups(g.childrens)
        case _ => Nil
      }

    findGroups(selectedShapes)
  }

  private def getTexts: List[ResizableText] = selectedShapes.flatMap {
    case x:ResizableText => List(x)
    case _ => Nil
  }

  private val linePatternToCssClass: Map[LinePattern.LinePattern, String] =
    LinePattern.linePatternToCssClass

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
    removeSelectedShape
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

  def getSelectedElements:List[ResizableShape] = selectedShapes

  def rotationMode(): Unit = {
    selectedShapes.foreach { shape =>
      shape.getAnchors.foreach(_.setVisible(false))
      changeLike.addNode(shape.rotationAnchors)
    }
  }

  def getMoveHandler: (MouseEvent => Unit) = {
    var mouseP = (0.0,0.0)
    var startP = mouseP

    def moveElement(mv: MouseEvent): Unit =
      (mv.getEventType, mv.getSource) match {
        case (MouseEvent.MOUSE_PRESSED, shape: MovableShape) =>
          mouseP = (mv.getSceneX,mv.getSceneY)
          startP = mouseP //save start-point for undo
        case (MouseEvent.MOUSE_DRAGGED, node: Node with MovableShape) =>
          //translate from original to new position
          val delta = (mv.getSceneX - mouseP.x, mv.getSceneY - mouseP.y)
          //if clicked shape is in selection:
          // move all selected
          //else: move only clicked shape
          withParentMovableElement(node) { shape =>
            val allShapes =
              if(selectedShapes.contains(shape)) selectedShapes
              else List(shape)

            allShapes.foreach(_.move(delta))
            //don't forget to use the new mouse-point as starting-point
            mouseP = (mv.getSceneX,mv.getSceneY)
          }
        case (MouseEvent.MOUSE_RELEASED, node: Node with MovableShape) =>
          withParentMovableElement(node) { shape =>
            val movedShapes =
              if(selectedShapes.contains(shape)) selectedShapes
              else List(shape)

            applySnapToGrid(node)

            //calculate delta (offset from original position) for un-/redo
            val deltaRedo = (mv.getSceneX - startP.x, mv.getSceneY - startP.y)
            val deltaUndo = deltaRedo.map(_*(-1))
            val cmd = History.
              newCommand(
                movedShapes.foreach(_.move(deltaRedo)),
                movedShapes.foreach(_.move(deltaUndo))
              )
            Global.history.save(cmd)
          }
        case _ => //unknown event
      }

    moveElement
  }

  private def applySnapToGrid(node:Node with MovableShape): Unit = {
    val delta = getSnapToGridDistance(node.getBoundsInParent.getMinX,
      node.getBoundsInParent.getMinY)

    node.move(delta)
  }

  /** Returns the delta for snap-to-grid for the point represented by (x,y). */
  private def getSnapToGridDistance(x:Double,y:Double):Point = {
    val deltaX = grid.getClosestXPosition(x).
      map (_.toDouble - x).getOrElse(0.0)

    val deltaY = grid.getClosestYPosition(y).
      map (_.toDouble - y).getOrElse(0.0)

    (deltaX,deltaY)
  }

  def removeSelectedShape: Unit = {
    for(shape <- selectedShapes) {
      changeLike.remove(shape.selectionRectangle)
    }
    selectedShapes = Nil
  }

  def deleteSelectedShape: Unit = {
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

  def setFillColorForSelectedShape(color:Color): Unit = if(!selectedShapes.isEmpty) {
    zippedUndo(coloredSelectedShape)(_.getFillColor)(
      _.setFillColor(color),
      _.setFillColor _
    )
  }

  def setStrokeColorForSelectedShape(color:Color): Unit = if(!selectedShapes.isEmpty) {
    zippedUndo(coloredSelectedShape)(_.getStrokeColor)(
      _.setStrokeColor(color),
      _.setStrokeColor _
    )
  }

  def setStrokeWidthForSelectedShape(width:Int): Unit = {
    zippedUndo(coloredSelectedShape)(_.getStrokeWidth)(
      _.setStrokeWidth(width),
      _.setStrokeWidth _
    )
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

    removeSelectedShape
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

  def setFontName(name:String): Unit = {
    zippedUndo(getTexts)(
      _.getFontName)(
      _.setFontName(name),
      _.setFontName _
    )
  }
  def setFontSize(size:Int): Unit =
    zippedUndo(getTexts)(
      _.getSize)(
      _.setSize(size),
      _.setSize _
    )

  def setFontColor(c:Color): Unit =
    zippedUndo(getTexts)(
      _.getFontColor)(
      _.setFontColor(c),
      _.setFontColor _
    )

  def setFontBold(b:Boolean): Unit =
    zippedUndo(getTexts)(_ => !b)(
      _.setBold(b),
      _.setBold _
    )

  def setFontItalic(b:Boolean): Unit =
    zippedUndo(getTexts)(_ => !b)(
      _.setItalic(b),
      _.setItalic _
    )

  def setFontUnderline(b:Boolean): Unit =
    zippedUndo(getTexts)(_ => !b)(
      _.setUnderline(b),
      _.setUnderline _
    )

  def setTextAlignment(alignment:TextAlignment): Unit =
    zippedUndo(getTexts)(_.getTextAlignment)(
      _.setTextAlignment(alignment),
      _.setTextAlignment _
    )
}
