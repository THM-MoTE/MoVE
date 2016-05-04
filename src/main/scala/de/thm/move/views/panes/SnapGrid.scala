/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views.panes

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.scene.shape.Line

import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.util.GeometryUtils

/** Creates a new grid with the size of the given topPane.
  * @param cellSize Size of each cell
  * @param snapDistance How close must the shape be so that the snapping mode is activate
  * */
class SnapGrid(topPane:Pane, val cellSize:Int, snapDistance:Int) extends Pane with SnapLike {

  val verticalLineId = "vertical-grid-line"
  val horizontalLineId = "horizontal-grid-line"

  val gridVisibleProperty = new SimpleBooleanProperty(true)
  val snappingProperty = new SimpleBooleanProperty(true)

  setPickOnBounds(false)
  getStyleClass.add("snap-grid-pane")

  prefHeightProperty.bind(topPane.prefHeightProperty)
  prefWidthProperty.bind(topPane.widthProperty)
  minHeightProperty.bind(topPane.minHeightProperty)
  minWidthProperty.bind(topPane.minWidthProperty)
  maxHeightProperty.bind(topPane.maxHeightProperty)
  maxWidthProperty.bind(topPane.maxWidthProperty)

  gridVisibleProperty.addListener { (oldB:java.lang.Boolean,newB:java.lang.Boolean) =>
    getChildren.clear()
    if(newB) {
      val horizontalLines = recalculateHorizontalLines(getHeight)
      val verticalLines = recalculateVerticalLines(getWidth)
      getChildren.addAll(horizontalLines:_*)
      getChildren.addAll(verticalLines:_*)
    }
    ()
  }

  heightProperty().addListener { (_:Number, newH:Number) =>
    if(gridVisibleProperty.get) {
      val newLines = recalculateHorizontalLines(newH.doubleValue())
      getChildren.removeIf { node:Node => node.getId == horizontalLineId }
      getChildren.addAll(newLines: _*)
    }
    ()
  }
  widthProperty().addListener { (_:Number, newW:Number) =>
    if(gridVisibleProperty.get) {
      val newLines = recalculateVerticalLines(newW.doubleValue())
      getChildren.removeIf { node:Node => node.getId == verticalLineId }
      getChildren.addAll(newLines:_*)
    }
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

  private def withSnappingMode[A](fn: => Option[A]): Option[A] = {
    if(gridVisibleProperty.get && snappingProperty.get) fn
    else None
  }

  private def getClosestPosition(maxV:Int, delta:Double): Option[Int] = withSnappingMode {
    GeometryUtils.closestMultiple(cellSize, delta) map(_.toInt) filter (x => Math.abs(delta-x) < snapDistance)
  }

  override def getClosestXPosition(deltaX:Double): Option[Int] = {
    val width = getWidth.toInt
    getClosestPosition(width,deltaX)
  }

  override  def getClosestYPosition(deltaY:Double): Option[Int] = {
    val height = getHeight.toInt
    getClosestPosition(height,deltaY)
  }

  def setCellSize(size:Int):SnapGrid = new SnapGrid(topPane, size, snapDistance)
}
