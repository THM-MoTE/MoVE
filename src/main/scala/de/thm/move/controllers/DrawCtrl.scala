/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.controllers

import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.input.{InputEvent, MouseEvent}
import javafx.scene.layout.Pane
import javafx.scene.paint.Color

import de.thm.move.Global
import de.thm.move.Global._
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

class DrawCtrl(drawPanel: DrawPanel, shapeInputHandler:InputEvent => Unit) {

  private var selectedShape:Option[ResizableShape] = None

  private val tmpShapeId = "temporary-shape"

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
          //start drawing; create tmp-shape
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
              c.setWidth(deltaX)
              c.setHeight(deltaY)
            case ba: BoundedAnchors =>
              ba.setWidth(deltaX)
              ba.setHeight(deltaY)
            case l: ResizableLine =>
              l.setEndX(newX)
              l.setEndY(newY)
            case _ => //ignore other shapes
          }
        case (_, MouseEvent.MOUSE_RELEASED, newP) =>
          //end drawing; remove temporary shape(s)
          removeTmpShapes(drawPanel, tmpShapeId)
          points.headOption foreach { start =>
            drawCustomShape(shape, start, newP)(fillColor, strokeColor, selectedThickness)
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
      case (n,_) => n.getId() == temporaryId
    }.map(_._1)

    node.getChildren.removeAll(removingNodes)
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
        addToPanel(shape)
        addToPanel(shape.getAnchors:_*)
      }
    }
  }

  def getMoveHandler: (MouseEvent => Unit) = {
    //var deltaX = -1.0
    //var deltaY = -1.0
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
      //add eventhandler
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

  def drawImage(img:Image): Unit = {
    val imgview = ShapeFactory.newImage(img)
    addToPanel(imgview)
    addToPanel(imgview.getAnchors:_*)
  }

  def drawCustomShape(shape:SelectedShape, start:Point, end:Point)(fillColor:Color, strokeColor:Color, selectedThickness:Int) = {
    val newShapeOpt:Option[ResizableShape] = shape match {
      case SelectedShape.Rectangle =>
        val (width,height) = end - start
        Some( ShapeFactory.newRectangle(start, width, height)(fillColor, strokeColor, selectedThickness) )
      case SelectedShape.Circle =>
        val (width, height) = (end - start) map GeometryUtils.asRadius
        val middlePoint = GeometryUtils.middleOfLine(start,end)
        Some( ShapeFactory.newCircle(middlePoint, width, height)(fillColor, strokeColor, selectedThickness) )
      case SelectedShape.Line => Some( ShapeFactory.newLine(start, end, selectedThickness)(fillColor, strokeColor, selectedThickness) )
      case _ => None
    }

    newShapeOpt foreach { x =>
      addToPanel(x)
      addToPanel(x.getAnchors:_*)
      //add shapes to focus-chain for getting keyboard-events
      x.setFocusTraversable(true)
      x.requestFocus()
    }
  }

  private def removeDrawnAnchors(cnt:Int):Unit =
    drawPanel.removeWhileIdx {
      case (shape, idx) => shape.isInstanceOf[Anchor] && idx<cnt
    }


  def setVisibilityOfAnchors(flag:Boolean): Unit = {
    drawPanel.getChildren.filter(_.isInstanceOf[Anchor]) foreach (  _.setVisible(flag) )
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
