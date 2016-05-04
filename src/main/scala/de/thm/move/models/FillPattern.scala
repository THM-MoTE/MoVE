/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.models

import javafx.scene._
import javafx.scene.paint._
import javafx.scene.canvas._
import javafx.scene.paint._
import javafx.scene.shape.Line

object FillPattern extends Enumeration {
  type FillPattern = Value
  val None, Solid, HorizontalCylinder, VerticalCylinder,
  Sphere, Horizontal, Vertical = Value

  def getFillColor(fillPattern:FillPattern.FillPattern,
    fillC:Color,
    strokeC:Color,
    width:Double,
    height:Double
    ):Paint = {
    /**
     * Implementation Nodes:
     * LinearGradients - Horizontal:
     *   create gradient from (bottom to middle); reflect them on
     *    (middle to top)
     * LinearGradients - Vertical:
     *   create gradient from (left to middle); reflect them on
     *    (middle to right)
     */
    val linearStops = List(
      new Stop(0,strokeC), //start with strokeColor
      //create a gentle transition to fillColor
      new Stop(0.45,fillC),
      new Stop(0.55,fillC),
      new Stop(1, strokeC) //end with strokeColor
    )
    val radialStops = List(
      new Stop(0.0, fillC),
      new Stop(0.20, fillC),
      new Stop(1, strokeC)
    )
    fillPattern match {
      case FillPattern.HorizontalCylinder =>
        new LinearGradient(0,0,0,1,true,CycleMethod.REFLECT, linearStops:_*)
      case FillPattern.VerticalCylinder =>
        new LinearGradient(0,0,1,0,true,CycleMethod.REFLECT, linearStops:_*)
      case FillPattern.Sphere =>
        new RadialGradient(0,0,0.5,0.5,1,true,CycleMethod.NO_CYCLE, radialStops:_*)
      case FillPattern.Solid => fillC
      case FillPattern.None => null //None = null = transparent color
      case FillPattern.Horizontal =>
        withCanvas(width,height,fillC) { canvas =>
          canvas.createHorizontalStructure(strokeC)
        }
      case FillPattern.Vertical =>
        withCanvas(width,height,fillC) { canvas =>
          canvas.createVerticalStructure(strokeC)
        }
      case _ => fillC
    }
  }

  private def withCanvas(width:Double,height:Double,fillC:Color)(fn: CustomCanvas => Unit): Paint = {
    val drawing = new CustomCanvas(width,height, fillC)
    fn(drawing)
    new ImagePattern(drawing.snapshot(null,null))
  }

  class CustomCanvas(width:Double,height:Double, backgroundColor: Color) extends Canvas(width, height) {
    val distance = 5 //distance between lines/cells
    val lineSize = 1
    val context = getGraphicsContext2D
    context.setLineWidth(lineSize)
    context.setFill(backgroundColor)
    context.fillRect(0,0, width,height)

    def createVerticalStructure(foreground:Color): Unit = {
      context.setFill(foreground)
      for (i <- 1 to (width/distance).toInt) yield {
        val startY = 0
        val endY = height
        val x = i*distance
        context.strokeLine(x,startY,x,endY)
        println("x: "+x)
      }
    }

    def createHorizontalStructure(foreground:Color): Unit = {
      context.setFill(foreground)
      for (i <- 1 to (height/distance).toInt) yield {
          val startX = 0
          val endX = width
          val y = i*distance
          context.strokeLine(startX,y,endX,y)
      }
    }
  }
}
