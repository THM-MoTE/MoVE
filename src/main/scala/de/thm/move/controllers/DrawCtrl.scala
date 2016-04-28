/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.controllers

import java.net.URI
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.{EventHandler, ActionEvent}
import javafx.scene.Node
import javafx.scene.input.{InputEvent, MouseEvent, KeyCode}
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.control.TextField
import javafx.scene.text.Font

import de.thm.move.Global
import de.thm.move.controllers.factorys.ShapeFactory
import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.history.History
import de.thm.move.history.History.Command
import de.thm.move.models.CommonTypes._
import de.thm.move.models.SelectedShape
import de.thm.move.models.SelectedShape._
import de.thm.move.util.GeometryUtils
import de.thm.move.util.PointUtils._
import de.thm.move.util.JFxUtils._
import de.thm.move.views.shapes._
import de.thm.move.views._
import scala.collection.JavaConversions._
import javafx.scene.Parent

/** Controller for drawing new shapes or adding existing shapes to the drawPanel. */
class DrawCtrl(changeLike:ChangeDrawPanelLike) {

  private val tmpShapeId = DrawPanel.tmpShapeId + "drawctrl"

  val drawConstraintProperty = new SimpleBooleanProperty()

  /** Signals that the running drawing-process should be aborted. */
  val abortDrawing = new SimpleBooleanProperty(false)

  def getDrawHandler: (SelectedShape, MouseEvent) => (Color, Color, Int) => Unit = {
    var points = List[Point]()
    var drawingShape: ResizableShape = null

    def drawHandler(shape:SelectedShape, mouseEvent:MouseEvent)(fillColor:Color, strokeColor:Color, selectedThickness:Int): Unit = {
      //reset draw-infos if process should be aborted
      if(abortDrawing.get) {
        points = Nil
        abortDrawing.set(false)
      }
      (shape, mouseEvent.getEventType, (mouseEvent.getX, mouseEvent.getY)) match {
        case (SelectedShape.Text,_,_ ) => throw new IllegalArgumentException("DrawCtrl.drawHandler can't draw text! Use DrawCtrl.drawText() instead!")
        case (SelectedShape.Polygon, MouseEvent.MOUSE_CLICKED, newP@(newX, newY)) =>
          //test if polygon is finish by checking if last clicked position is 1st clicked point
          points.lastOption match {
            case Some((x, y)) if Math.abs(x - newX) <= 10 && Math.abs(y - newY) <= 10 =>
              drawPolygon(points)(fillColor, strokeColor, selectedThickness)
              points = List()
              removeTmpShapes(tmpShapeId)
            case _ =>
              //draw tmp line between last anchor and mouse point
              (points, drawingShape) match {
                case ((hdX, hdY) :: _, l: ResizableLine) =>
                  //draw line between last anchor (head of list) and mouse point
                  l.setEndX(newX)
                  l.setEndY(newY)
                case _ =>
              }
              //create new line with this mouse point as start point
              drawingShape = createTmpShape(SelectedShape.Line, newP, strokeColor)

              points = newP :: points
              drawTmpAnchor(points.head)
          }
        case (SelectedShape.Path, MouseEvent.MOUSE_CLICKED, _)  if mouseEvent.getClickCount == 2 =>
          /* Point of double-click already added to points because the eventhandler
            get's double-executed with the same point from javafx:
            1. clickCount = 1|0 (branch below)
            2. clickCount = 2 (this branch)
           */
          drawPath(points)(fillColor, strokeColor, selectedThickness)
          points = List()
          removeTmpShapes(tmpShapeId)
        case (SelectedShape.Path, MouseEvent.MOUSE_CLICKED, newP@(newX, newY)) =>
          //draw tmp line between last anchor and mouse point
          (points, drawingShape) match {
            case ((hdX, hdY) :: _, l: ResizableLine) =>
              //draw line between last anchor (head of list) and mouse point
              l.setEndX(newX)
              l.setEndY(newY)
            case _ =>
          }
          //create new line with this mouse point as start point
          drawingShape = createTmpShape(SelectedShape.Line, newP, strokeColor)
          points = newP :: points
          drawTmpAnchor(points.head)
        case (SelectedShape.Path, _,_) | (SelectedShape.Polygon, _, _) => //ignore other path/polygon events
        case (_, MouseEvent.MOUSE_PRESSED, newP) =>
          //start drawing
          drawingShape = createTmpShape(shape, newP, strokeColor)
          points = newP :: points
        case (_, MouseEvent.MOUSE_DRAGGED, newP@(newX, newY)) =>
          //adjust tmp-figure
          val startP = points.head
          adjustTmpFigure(drawingShape, startP, newP)
        case (_, MouseEvent.MOUSE_RELEASED, newP) =>
          //end drawing
          removeTmpShapes(tmpShapeId)
          points.headOption foreach { start =>
            drawCustomShape(shape, start, newP, drawConstraintProperty.get)(fillColor, strokeColor, selectedThickness)
          }
          points = List()
        case _ => //ignore all other
      }
    }

    drawHandler
  }

  private def adjustTmpFigure(drawingShape:ResizableShape, startP:Point, newP:Point): Unit = {
    val (deltaX, deltaY) = newP - startP
    val (newX,newY) = newP
    drawingShape match {
      case c: ResizableCircle =>
        val (middleX, middleY) = GeometryUtils.middleOfLine(startP, newP)
        c.setX(middleX)
        c.setY(middleY)
        if(drawConstraintProperty.get) {
          val tmpDelta = deltaX min deltaY
          c.setWidth(tmpDelta)
          c.setHeight(tmpDelta)
        } else {
          c.setWidth(deltaX)
          c.setHeight(deltaY)
        }
      case ba: RectangleLike =>
        if(drawConstraintProperty.get) {
          val tmpDelta = deltaX min deltaY
          ba.setWidth(tmpDelta)
          ba.setHeight(tmpDelta)
        } else {
          ba.setWidth(deltaX)
          ba.setHeight(deltaY)
        }
      case l: ResizableLine =>
        if(drawConstraintProperty.get) {
          val (startX,startY) = startP
          val (x,y) = if(deltaX > deltaY) (newX,startY) else (startX,newY)
          l.setEndX(x)
          l.setEndY(y)
        } else {
          l.setEndX(newX)
          l.setEndY(newY)
        }
      case _ => //ignore other shapes
    }
  }

  /**Creates a temporary shape and adds it to the given node for displaying during drawing a shape.*/
  private def createTmpShape(selectedShape:SelectedShape.SelectedShape, start:Point, stroke:Color, shapeId:String = tmpShapeId): ResizableShape = {
    val shape = ShapeFactory.createTemporaryShape(selectedShape, start)(stroke)
    shape.setId(shapeId)
    changeLike.addNode(shape)
    shape
  }

  /**Removes all temporary shapes (identified by temporaryId) from the given node.*/
  private def removeTmpShapes(temporaryId:String): Unit = {
    val removingNodes = changeLike.getElements.filter {
      n => n.getId == temporaryId
    }
    removingNodes foreach changeLike.remove
  }

  def abortDrawingProcess(): Unit = {
    removeTmpShapes(tmpShapeId)
    abortDrawing.set(true)
  }

  def drawTmpAnchor(p:Point): Unit = {
    val anchor = ShapeFactory.newAnchor(p)
    anchor.setId(tmpShapeId)
    changeLike.addNode(anchor)
  }

  def drawText(x:Double,y:Double,color:Color,font:Font): Unit = {
    val text = new TextField()
    text.setId(tmpShapeId)
    text.setOnAction { _:ActionEvent =>
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

  def drawPolygon(points:List[Point])(fillColor:Color, strokeColor:Color, selectedThickness: Int) = {
    val polygon = ShapeFactory.newPolygon(points)(fillColor, strokeColor, selectedThickness)
    removeDrawnAnchors(points.size)
    changeLike.addShape(polygon)
    changeLike.addNode(polygon.getAnchors)
  }

  def drawPath(points:List[Point])(fillColor:Color, strokeColor:Color, selectedThickness: Int) = {
    val path = ShapeFactory.newPath(points)(fillColor, strokeColor, selectedThickness)
    removeDrawnAnchors(points.size)
    changeLike.addShape(path)
    changeLike.addNode(path.getAnchors)
  }

  def drawImage(imgUri:URI): Unit = {
    val imgview = ShapeFactory.newImage(imgUri)
    changeLike.addShape(imgview)
    changeLike.addNode(imgview.getAnchors)
  }

  def drawCustomShape(shape:SelectedShape, start:Point, end:Point, drawConstraint:Boolean)(fillColor:Color, strokeColor:Color, selectedThickness:Int) = {
    val newShapeOpt:Option[ResizableShape] = shape match {
      case SelectedShape.Rectangle if drawConstraint =>
        val (width,height) = end - start
        val min = width min height
        Some( ShapeFactory.newRectangle(start, min, min)(fillColor, strokeColor, selectedThickness) )
      case SelectedShape.Rectangle if !drawConstraint =>
        val (width,height) = end - start
        Some( ShapeFactory.newRectangle(start, width, height)(fillColor, strokeColor, selectedThickness) )
      case SelectedShape.Circle if drawConstraint =>
        val (width, height) = (end - start) map GeometryUtils.asRadius
        val min = width min height
        val middlePoint = GeometryUtils.middleOfLine(start,end)
        Some( ShapeFactory.newCircle(middlePoint, min, min)(fillColor, strokeColor, selectedThickness) )
      case SelectedShape.Circle if !drawConstraint =>
        val (width, height) = (end - start) map GeometryUtils.asRadius
        val middlePoint = GeometryUtils.middleOfLine(start,end)
        Some( ShapeFactory.newCircle(middlePoint, width, height)(fillColor, strokeColor, selectedThickness) )
      case SelectedShape.Line if drawConstraint =>
        val (deltaX,deltaY) = end - start
        val (startX,startY) = start
        val (endX,endY) = end
        val newEnd = if(deltaX > deltaY) (endX,startY) else (startX, endY)
        Some( ShapeFactory.newLine(start, newEnd, selectedThickness)(fillColor, strokeColor, selectedThickness) )
      case SelectedShape.Line if !drawConstraint =>
        Some( ShapeFactory.newLine(start, end, selectedThickness)(fillColor, strokeColor, selectedThickness) )
      case _ => None
    }

    newShapeOpt foreach { x =>
      changeLike.addShape(x)
      changeLike.addNode(x.getAnchors)
    }
  }

  private def removeDrawnAnchors(cnt:Int):Unit = {
    val anchors = changeLike.getElements.filter(_.isInstanceOf[Anchor])
    anchors.reverse.take(cnt) foreach changeLike.remove
  }
}
