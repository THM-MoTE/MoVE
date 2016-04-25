/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.models

import javafx.scene.paint._

object FillPattern extends Enumeration {
  type FillPattern = Value
  val None, Solid, HorizontalCylinder, VerticalCylinder, Sphere = Value

  def getFillColor(fillPattern:FillPattern.FillPattern, fillC:Color, strokeC:Color):Paint = {
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
      case _ => fillC
    }
  }
}
