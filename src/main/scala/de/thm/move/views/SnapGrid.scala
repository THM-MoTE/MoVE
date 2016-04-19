package de.thm.move.views

import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import scala.collection.JavaConversions._

import de.thm.move.controllers.implicits.FxHandlerImplicits._

class SnapGrid(topPane:Pane, cellSize:Int, snapDistance:Int) extends Pane {

  val verticalLineId = "vertical-grid-line"
  val horizontalLineId = "horizontal-grid-line"

  setPickOnBounds(false)
  getStyleClass.add("snap-grid-pane")

  heightProperty().addListener { (_:Number, newH:Number) =>
    val newLines = recalculateHorizontalLines(newH.doubleValue())
    getChildren.removeIf { node:Node => node.getId == horizontalLineId }
    getChildren.addAll(newLines: _*)
    ()
  }
  widthProperty().addListener { (_:Number, newW:Number) =>
    val newLines = recalculateVerticalLines(newW.doubleValue())
    getChildren.removeIf { node:Node => node.getId == verticalLineId }
    getChildren.addAll(newLines:_*)
    ()
  }

  def recalculateHorizontalLines(height:Double): Seq[Line] =
    for(i <- 1 to (height/cellSize).toInt) yield {
      val line = newGridLine
      line.setId(horizontalLineId)
      line.setStartX(0)
      line.endXProperty().bind(widthProperty())
      val y = i*cellSize
      line.setStartY(y)
      line.setEndY(y)
      line
    }

  def recalculateVerticalLines(width:Double): Seq[Line] =
    for(i <- 1 to (width/cellSize).toInt) yield {
      val line = newGridLine
      line.setId(verticalLineId)
      line.setStartY(0)
      line.endYProperty().bind(heightProperty())
      val x = i*cellSize
      line.setStartX(x)
      line.setEndX(x)
      line
    }

  def newGridLine: Line = {
    val line = new Line()
    line.getStyleClass.add("grid-line")
    line
  }

  def getClosestXPosition(deltaX:Double): Option[Int] = {
    val width = getWidth.toInt
    (cellSize to  width by cellSize).foldLeft[Option[Int]](Some(-1)) {
      case (Some(-1), e) => Some(e) //it's the start value
      case (None, _) => None
      case (Some(x), e) =>
        if(Math.abs(deltaX - e) == Math.abs(deltaX - x)) None //deltaX is in the middle of 2 lines
        else if(Math.abs(deltaX - e) < Math.abs(deltaX - x)) Some(e)
        else if(Math.abs(deltaX - x) < Math.abs(deltaX - e)) Some(x)
        else None
    } filter {
      x =>
        println("x: "+x)
        println("abs: "+Math.abs(deltaX-x))
        Math.abs(deltaX-x) <= snapDistance
    }
  }
}
