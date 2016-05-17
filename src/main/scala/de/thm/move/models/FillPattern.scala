/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.models

import javafx.scene.canvas._
import javafx.scene.paint._

import de.thm.move.Global._
object FillPattern extends Enumeration {
  type FillPattern = Value
  val None, Solid, HorizontalCylinder, VerticalCylinder,
  Sphere, Horizontal, Vertical, Cross, Forward, Backward, CrossDiag = Value

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
      case FillPattern.Cross =>
        withCanvas(width,height,fillC) { canvas =>
          canvas.createGridStructure(strokeC)
        }
      case FillPattern.Forward =>
        withCanvas(width,height,fillC) { canvas =>
          canvas.createForwardStructure(strokeC)
        }
      case FillPattern.Backward =>
        withCanvas(width,height,fillC) { canvas =>
          canvas.createBackwardStructure(strokeC)
        }
      case FillPattern.CrossDiag =>
        withCanvas(width,height,fillC) { canvas =>
          canvas.createCrossDiagStructure(strokeC)
        }
      case _ => fillC
    }
  }

  private def withCanvas(width:Double,height:Double,fillC:Color)(fn: CustomCanvas => Unit): Paint = {
    val drawing = new CustomCanvas(width,height, fillC)
    fn(drawing)
    new ImagePattern(drawing.snapshot(null,null))
  }

  /** A canvas for drawing modelica's structure-like FillPatterns.
    */
  class CustomCanvas(width:Double,height:Double, backgroundColor: Color) extends Canvas(width, height) {
    val distance = config.getInt("structure-distance").getOrElse(5) //distance between lines/cells
    val lineSize = config.getInt("structure-linesize").getOrElse(1)
    val context = getGraphicsContext2D
    context.setLineWidth(lineSize)
    context.setFill(backgroundColor)
    context.fillRect(0,0, width,height) //create a backgroundColor

    /** Draws the FillPattern.Vertical structure from modelica onto this canvas
      * with the given color.
      */
    def createVerticalStructure(foreground:Color): Unit = {
      context.setFill(foreground)
        //creates vertical lines starting left going to right
      for (i <- 1 to (width/distance).toInt) yield {
        val startY = 0
        val endY = height
        val x = i*distance
        context.strokeLine(x,startY,x,endY)
      }
    }

    /** Draws the FillPattern.Horizontal structure from modelica onto this canvas
      * with the given color.
      */
    def createHorizontalStructure(foreground:Color): Unit = {
      context.setFill(foreground)
      for (i <- 1 to (height/distance).toInt) yield {
          val startX = 0
          val endX = width
          val y = i*distance
          context.strokeLine(startX,y,endX,y)
      }
    }

    /** Draws the FillPattern.Cross structure from modelica onto this canvas
      * with the given color.
      */
    def createGridStructure(foreground:Color): Unit = {
      createVerticalStructure(foreground)
      createHorizontalStructure(foreground)
    }

    /** Draws the FillPattern.Backward structure from modelica onto this canvas
      * with the given color.
      */
    def createBackwardStructure(foreground:Color): Unit = {
      context.setFill(foreground)
      val max = width max height
      //create lines going from top-left to bottom-right
      for(i <- 1 to ((max*2)/distance).toInt) yield {
        val x = i*distance
        val y = i*distance
        context.strokeLine(0, y, x, 0)
      }
    }

    /** Draws the FillPattern.Forward structure from modelica onto this canvas
      * with the given color.
      */
    def createForwardStructure(foreground:Color): Unit = {
      context.setFill(foreground)
      val max = width max height
      val doubled = max*2
      var endX = 0 //distance to last line
        //create lines from bottom-left to middle
      for(i <- (doubled/distance).toInt to 0 by -1) yield {
        val startX = 0
        val startY = i*distance
        val endY = doubled
        context.strokeLine(startX, startY, endX, endY)
        endX += 5 //5px between this and the next line
      }

        //create lines from middle to top-right
      for(i <- 1 to (doubled/distance).toInt) yield {
        val startX = i*distance
        val startY = 0
        val endY = doubled
        context.strokeLine(startX, startY, endX, endY)
        endX += 5
      }
    }

    /** Draws the FillPattern.Cross structure from modelica onto this canvas
      * with the given color.
      */
    def createCrossDiagStructure(foreground:Color): Unit = {
      createBackwardStructure(foreground)
      createForwardStructure(foreground)
    }
  }
}
