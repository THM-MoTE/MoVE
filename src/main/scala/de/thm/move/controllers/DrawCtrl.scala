/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.controllers

import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.image.{Image, ImageView}
import javafx.scene.input.{InputEvent, MouseEvent}
import javafx.scene.paint.Color
import javafx.scene.shape.{Rectangle, Shape}
import javafx.scene.input.KeyEvent

import de.thm.move.Global
import de.thm.move.controllers.factorys.ShapeFactory
import de.thm.move.history.History
import de.thm.move.models.CommonTypes._
import de.thm.move.models.SelectedShape
import de.thm.move.models.SelectedShape._
import de.thm.move.views.shapes.{ColorizableShape, ResizableShape}
import de.thm.move.views.{Anchor, DrawPanel}
import de.thm.move.history.History.{Action, Command}

import collection.JavaConversions._

class DrawCtrl(drawPanel: DrawPanel, shapeInputHandler:InputEvent => Unit) {

  private var selectedShape:Option[ResizableShape] = None

  def getDrawHandler: (SelectedShape, MouseEvent) => (Color, Color, Int) => Unit = {
    var points = List[Point]()

    def drawHandler(shape:SelectedShape, mouseEvent:MouseEvent)(fillColor:Color, strokeColor:Color, selectedThickness:Int): Unit = {
      shape match {
        case SelectedShape.Polygon =>
          if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
            val newX = mouseEvent.getX()
            val newY = mouseEvent.getY()
            //test if polygon is finish by checking if last clicked position is 1st clicked point
            points.reverse.headOption match {
              case Some((x,y)) if Math.abs(x - newX) <= 10 && Math.abs(y - newY) <= 10 =>
                //draw the polygon
                drawPolygon(points)(fillColor, strokeColor, selectedThickness)
                points = List()
              case _ =>
                points = (newX, newY) :: points
                drawAnchor(points.head)
            }
          }
        case _ =>
          if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
            points = (mouseEvent.getX(), mouseEvent.getY()) :: points
          } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
            points = (mouseEvent.getX(), mouseEvent.getY()) :: points

            points match {
              case end :: start :: _ => drawCustomShape(shape, start, end)(fillColor, strokeColor, selectedThickness)
              case _ => //ignore
            }
            points = List()
          }
      }
    }

    drawHandler
  }

  def setSelectedShape(shape:ResizableShape): Unit = {
    println(s"set selected $selectedShape")
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
    selectedShape match {
      case Some(shape) =>
        drawPanel.getChildren.remove(shape.selectionRectangle)
        selectedShape = None
      case _ => //ignore
    }
  }

  def getMoveHandler: (MouseEvent => Unit) = {
    var deltaX = -1.0
    var deltaY = -1.0

    var command: (=> Unit) => Command = x => { History.emptyAction }

    def moveElement(mv:MouseEvent): Unit = {
      //move selected element
      mv.getEventType match {
        case MouseEvent.MOUSE_PRESSED =>
          mv.getSource match {
            case shape:ResizableShape =>
              //save old coordinates for un-/redo
              val oldX = shape.getX
              val oldY = shape.getY

              command = History.partialAction {
                println("undo action")
                shape.setX(oldX)
                shape.setY(oldY)
              }

              deltaX = oldX - mv.getSceneX
              deltaY = oldY - mv.getSceneY
            case _:Anchor => //ignore, will be repositioned when moving the shape
            case _ => throw new IllegalStateException("shapeInputHandler: source isn't a shape")
          }
        case MouseEvent.MOUSE_DRAGGED =>
          //translate from original to new position
          mv.getSource match {
            case shape:ResizableShape =>
              shape.setX(deltaX + mv.getSceneX)
              shape.setY(deltaY + mv.getSceneY)
            case _:Anchor => //ignore, will be repositioned when moving the shape
            case _ => throw new IllegalStateException("shapeInputHandler: source isn't a shape")
          }
        case MouseEvent.MOUSE_RELEASED =>
          mv.getSource match {
            case shape:ResizableShape =>
              val newX = shape.getX
              val newY = shape.getY
              val cmd = command {
                println("redo action")
                shape.setX(newX)
                shape.setY(newY)
              }

              println("saving action")

              Global.history.save(cmd)

            case _ => //ignore
          }
        case _ => //unknown event
      }
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
    val (startX, startY) = start
    val (endX, endY) = end

    val newShapeOpt:Option[ResizableShape] = shape match {
      case SelectedShape.Rectangle =>
        val width = endX - startX
        val height = endY - startY
        Some( ShapeFactory.newRectangle(start, width, height)(fillColor, strokeColor, selectedThickness) )
      case SelectedShape.Circle =>
        val width = endX - startX
        val height = endY - startY
        Some( ShapeFactory.newCircle(start, width, height)(fillColor, strokeColor, selectedThickness) )
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

  private def removeDrawnAnchors(cnt:Int):Unit = {
    val removingAnchors = drawPanel.getShapes.zipWithIndex.takeWhile {
      case (shape, idx) => shape.isInstanceOf[Anchor] && idx<cnt
    }.map(_._1)

    //remove from shapelist
    drawPanel.setShapes( drawPanel.getShapes.zipWithIndex.dropWhile {
      case (shape, idx) => shape.isInstanceOf[Anchor] && idx<cnt
    }.map(_._1) )

    //remove from painting area
    drawPanel.getChildren.removeAll(removingAnchors:_*)
  }


  def setVisibilityOfAnchors(flag:Boolean): Unit = {
    drawPanel.getChildren.filter(_.isInstanceOf[Anchor]) foreach { anchor =>
      anchor.setVisible(flag)
    }
  }

  def changeColorForSelectedShape(fillColor:Option[Color], strokeColor:Option[Color]): Unit = {
    selectedShape flatMap {
      case x:ColorizableShape => Some(x)
      case _ => None
    } foreach { x =>
      fillColor foreach ( x.setFillColor(_) )
      strokeColor foreach ( x.setStrokeColor(_) )
    }
  }

  def changeStrokeWidthForSelectedShape(width:Int): Unit = {
    selectedShape flatMap {
      case x:ColorizableShape => Some(x)
      case _ => None
    } foreach { x =>
      x.setStrokeWidth(width)
    }
  }
}
