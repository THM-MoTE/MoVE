/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.controllers

import java.net.URI
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.input.{InputEvent, MouseEvent}
import javafx.scene.layout.Pane
import javafx.scene.paint.Color

import de.thm.move.Global
import de.thm.move.controllers.factorys.ShapeFactory
import de.thm.move.history.History
import de.thm.move.history.History.Command
import de.thm.move.models.CommonTypes._
import de.thm.move.models.SelectedShape
import de.thm.move.models.SelectedShape._
import de.thm.move.util.GeometryUtils
import de.thm.move.util.PointUtils._
import de.thm.move.views.shapes._
import de.thm.move.views.{Anchor, DrawPanel}

import scala.collection.JavaConversions._

class DrawCtrl(val drawPanel: DrawPanel, shapeInputHandler:InputEvent => Unit) extends SelectableShapeCtrl {

  private val tmpShapeId = "temporary-shape"

  val drawConstraintProperty = new SimpleBooleanProperty()

  def getDrawHandler: (SelectedShape, MouseEvent) => (Color, Color, Int) => Unit = {
    var points = List[Point]()
    var drawingShape: ResizableShape = null

    def drawHandler(shape:SelectedShape, mouseEvent:MouseEvent)(fillColor:Color, strokeColor:Color, selectedThickness:Int): Unit = {
      (shape, mouseEvent.getEventType, (mouseEvent.getX, mouseEvent.getY)) match {
        case (SelectedShape.Polygon, MouseEvent.MOUSE_CLICKED, newP@(newX, newY)) =>
          //test if polygon is finish by checking if last clicked position is 1st clicked point
          points.lastOption match {
            case Some((x, y)) if Math.abs(x - newX) <= 10 && Math.abs(y - newY) <= 10 =>
              drawPolygon(points)(fillColor, strokeColor, selectedThickness)
              points = List()
              removeTmpShapes(drawPanel, tmpShapeId)
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
              drawingShape = createTmpShape(SelectedShape.Line, (mouseEvent.getX, mouseEvent.getY), strokeColor, drawPanel)

              points = newP :: points
              drawAnchor(points.head)
          }
        case (SelectedShape.Polygon, _, _) => //ignore other polygon events
        case (_, MouseEvent.MOUSE_PRESSED, newP) =>
          //start drawing
          drawingShape = createTmpShape(shape, newP, strokeColor, drawPanel)
          points = newP :: points
        case (_, MouseEvent.MOUSE_DRAGGED, newP@(newX, newY)) =>
          //adjust tmp-figure
          val startP = points.head
          val (deltaX, deltaY) = newP - startP
          drawingShape match {
            case c: ResizableCircle =>
              val (middleX, middleY) = GeometryUtils.middleOfLine(points.head, newP)
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
            case ba: BoundedAnchors =>
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
        case (_, MouseEvent.MOUSE_RELEASED, newP) =>
          //end drawing
          removeTmpShapes(drawPanel, tmpShapeId)
          points.headOption foreach { start =>
            drawCustomShape(shape, start, newP, drawConstraintProperty.get)(fillColor, strokeColor, selectedThickness)
          }
          points = List()
        case _ => //ignore all other
      }
    }

    drawHandler
  }

  /**Creates a temporary shape and adds it to the given node for displaying during drawing a shape.*/
  private def createTmpShape(selectedShape:SelectedShape.SelectedShape, start:Point, stroke:Color, node:Pane, shapeId:String = tmpShapeId): ResizableShape = {
    val shape = ShapeFactory.createTemporaryShape(selectedShape, start)(stroke)
    shape.setId(shapeId)
    node.getChildren.add(shape)
    shape
  }

  /**Removes all temporary shapes (identified by temporaryId) from the given node.*/
  private def removeTmpShapes(node:Pane, temporaryId:String): Unit = {
    val removingNodes = node.getChildren.zipWithIndex.filter {
      case (n,_) => n.getId == temporaryId
    }.map(_._1)

    node.getChildren.removeAll(removingNodes)
  }

  def getMoveHandler: (MouseEvent => Unit) = {
    var delta = (-1.0,-1.0)

    var command: (=> Unit) => Command = x => { History.emptyAction }

    def moveElement(mv: MouseEvent): Unit =
      (mv.getEventType, mv.getSource) match {
        case (MouseEvent.MOUSE_PRESSED, shape: ResizableShape) =>
          //save old coordinates for undo
          val old = shape.getXY

          command = History.partialAction {
            shape.setXY(old)
          }

          delta = old - (mv.getSceneX,mv.getSceneY)
        case (MouseEvent.MOUSE_DRAGGED, shape: ResizableShape) =>
          //translate from original to new position
          shape.setXY(delta + (mv.getSceneX, mv.getSceneY))
        case (MouseEvent.MOUSE_RELEASED, shape: ResizableShape) =>
          //save coordinates for redo
          val newP = shape.getXY
          val cmd = command {
            shape.setXY(newP)
          }

          Global.history.save(cmd)
        case _ => //unknown event
      }

    moveElement
  }

  def addToPanel[T <: Node](shape:T*): Unit = {
    shape foreach { x =>
      x.addEventHandler(InputEvent.ANY, new EventHandler[InputEvent]() {
        override def handle(event: InputEvent): Unit = shapeInputHandler(event)
      })
      drawPanel.drawShape(x)
    }
  }

  def drawAnchor(p:Point): Unit = {
    val anchor = ShapeFactory.newAnchor(p)
    drawPanel.drawShape(anchor)
  }

  def drawPolygon(points:List[Point])(fillColor:Color, strokeColor:Color, selectedThickness: Int) = {
    val polygon = ShapeFactory.newPolygon(points)(fillColor, strokeColor, selectedThickness)
    removeDrawnAnchors(points.size)
    addToPanel(polygon)
    addToPanel(polygon.getAnchors:_*)
  }

  def drawImage(imgUri:URI): Unit = {
    val imgview = ShapeFactory.newImage(imgUri)
    addToPanel(imgview)
    addToPanel(imgview.getAnchors:_*)
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
      addToPanel(x)
      addToPanel(x.getAnchors:_*)
    }
  }

  private def removeDrawnAnchors(cnt:Int):Unit =
    drawPanel.removeWhileIdx {
      case (shape, idx) => shape.isInstanceOf[Anchor] && idx<cnt
    }


  def setVisibilityOfAnchors(flag:Boolean): Unit = {
    drawPanel.getChildren.filter(_.isInstanceOf[Anchor]) foreach (  _.setVisible(flag) )
  }
}
