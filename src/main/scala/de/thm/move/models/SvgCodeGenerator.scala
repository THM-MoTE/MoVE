/**
  * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
  */

package de.thm.move.models

import java.nio.file.{Files, Path}
import javafx.scene.Node

import de.thm.move.Global._
import de.thm.move.views.shapes.ResizableShape

import scala.xml.{Elem, NodeSeq}

class SvgCodeGenerator {

  def generateShapes(shapes:List[Node], width:Double, height:Double): Elem = {
    <svg width="@width" height="@height">
      shapes.map(generateShape)
    </svg>
  }
  def generateShape(shape:Node): Elem = shape match {
    case _ => <node></node>
  }

  def writeToFile(str:String)(target:Path): Unit = {
    val writer = Files.newBufferedWriter(target, encoding)
  }
}
