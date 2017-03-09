package de.thm.move.models.pattern

import javafx.scene.canvas.Canvas
import javafx.scene.paint._

import de.thm.move.Global._
import de.thm.move.types._

trait FillPattern extends ModelicaPattern {
  override def modelicaRepresentation: String = s"FillPattern.${patternName}"

  protected def getPaint(fillC:Color,
                         strokeC:Color,
                         width:Double,
                         height:Double):Paint

  override def applyToShape(shape:ColorizableNode): Unit = {
    val paint = getPaint(
      shape.oldFillColorProperty.get,
      shape.getStrokeColor.asInstanceOf[Color],
      shape.getBoundsInLocal.getWidth,
      shape.getBoundsInLocal.getHeight)
    shape.setFillColor(paint)
  }
}

case object FNone extends FillPattern {
  override protected def getPaint(fillC: Color, strokeC: Color, width: Double, height: Double): Paint = null //= transparent color
  override def patternName:String = "None"
}
case object FSolid extends FillPattern {
  override protected def getPaint(fillC: Color, strokeC: Color, width: Double, height: Double): Paint = fillC
  override def patternName:String = "Solid"
}

private[pattern] trait CylinderPattern {
  protected def linearStops(fillC:Color, strokeC:Color) = List(
    new Stop(0,strokeC), //start with strokeColor
    //create a gentle transition to fillColor
    new Stop(0.45,fillC),
    new Stop(0.55,fillC),
    new Stop(1, strokeC) //end with strokeColor
  )
}


case object HorizontalCylinder extends FillPattern with CylinderPattern {
  /**
   * Implementation Node:
   * LinearGradients - HorizontalCylinder:
   *   create gradient from (bottom to middle); reflect them on
   *    (middle to top)
   */

  override protected def getPaint(fillC: Color, strokeC: Color, width: Double, height: Double): Paint =
    new LinearGradient(0,0,0,1,true,CycleMethod.REFLECT, linearStops(fillC, strokeC):_*)
}
case object VerticalCylinder extends FillPattern with CylinderPattern {
  /** Implementation Node:
    * LinearGradients - VerticalCylinder:
    *   create gradient from (left to middle); reflect them on
    *    (middle to right)
   */

  override protected def getPaint(fillC: Color, strokeC: Color, width: Double, height: Double): Paint =
    new LinearGradient(0,0,1,0,true,CycleMethod.REFLECT, linearStops(fillC, strokeC):_*)
}
case object Sphere extends FillPattern {
  def radialStops(fillC:Color, strokeC:Color) = List(
    new Stop(0.0, fillC),
    new Stop(0.20, fillC),
    new Stop(1, strokeC)
  )

  override protected def getPaint(fillC: Color, strokeC: Color, width: Double, height: Double): Paint =
    new RadialGradient(0,0,0.5,0.5,1,true,CycleMethod.NO_CYCLE, radialStops(fillC, strokeC):_*)
}
case object Horizontal extends FillPattern {
  override protected def getPaint(fillC: Color, strokeC: Color, width: Double, height: Double): Paint =
    FillPattern.withCanvas(width, height, fillC)( _.createHorizontalStructure(strokeC) )
}
case object Vertical extends FillPattern {
  override protected def getPaint(fillC: Color, strokeC: Color, width: Double, height: Double): Paint =
    FillPattern.withCanvas(width, height, fillC)( _.createVerticalStructure(strokeC) )
}
case object Cross extends FillPattern {
  override protected def getPaint(fillC: Color, strokeC: Color, width: Double, height: Double): Paint =
    FillPattern.withCanvas(width, height, fillC)( _.createGridStructure(strokeC) )
}
case object Forward extends FillPattern {
  override protected def getPaint(fillC: Color, strokeC: Color, width: Double, height: Double): Paint =
    FillPattern.withCanvas(width, height, fillC)( _.createForwardStructure(strokeC) )
}
case object Backward extends FillPattern {
  override protected def getPaint(fillC: Color, strokeC: Color, width: Double, height: Double): Paint =
    FillPattern.withCanvas(width, height, fillC)( _.createBackwardStructure(strokeC) )
}
case object CrossDiag extends FillPattern {
  override protected def getPaint(fillC: Color, strokeC: Color, width: Double, height: Double): Paint =
    FillPattern.withCanvas(width, height, fillC)( _.createCrossDiagStructure(strokeC) )
}

object FillPattern {

  val patternObjects:List[FillPattern] =
    List(FNone, FSolid, HorizontalCylinder, VerticalCylinder, Sphere, Horizontal,
          Vertical, Cross, Forward, Backward, CrossDiag)

  private[pattern] def withCanvas(width:Double,height:Double,fillC:Color)(fn: CustomCanvas => Unit): Paint = {
    val drawing = new CustomCanvas(width,height, fillC)
    fn(drawing)
    new ImagePattern(drawing.snapshot(null,null))
  }

  /** A canvas for drawing modelica's structure-like FillPatterns.
    */
  private[pattern] class CustomCanvas(width:Double,height:Double, backgroundColor: Color) extends Canvas(width, height) {
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
