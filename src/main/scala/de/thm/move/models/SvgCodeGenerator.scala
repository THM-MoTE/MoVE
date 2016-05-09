/**
  * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
  */

package de.thm.move.models

import java.nio.file.{Files, Path}
import javafx.scene.Node

import de.thm.move.Global._
import de.thm.move.views.shapes._

import scala.xml.{Elem, NodeSeq}

class SvgCodeGenerator {

  def generateShapes(shapes:List[Node], width:Double, height:Double): Elem = {
    <svg width={width.toString} height={height.toString}>
      {shapes.map(generateShape)}
    </svg>
  }
  def generateShape(shape:Node): Elem = shape match {
    case rect:ResizableRectangle => genRectangle(rect)
    case _ => throw new IllegalArgumentException(s"Can't generate svg code for: $shape")
  }

  private def genRectangle(rectangle:ResizableRectangle): Elem = {
    <rect
      x={rectangle.getX.toString}
      y={rectangle.getY.toString}
      width={rectangle.getWidth.toString}
      height={rectangle.getHeight.toString}
      />
  }

  def writeToFile(str:String)(target:Path): Unit = {
    val writer = Files.newBufferedWriter(target, encoding)

    try {
      writer.write(str)
    } finally {
      writer.close()
    }
  }
}
