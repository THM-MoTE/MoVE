/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.controllers

import javafx.scene.paint.Color

import de.thm.move.Global._
import de.thm.move.models.pattern.{LinePattern, FillPattern}
import de.thm.move.views.GroupLike
import de.thm.move.views.shapes.{ColorizableShape, ResizableShape}

import scala.collection.JavaConversions._

/** Behaviour for colorizing selected ResizableShapes. */
trait ColorizeSelectionCtrl {
  this:SelectionCtrlLike =>

  /** Gets all shapes that are colorizable and removes groups if they exist */
  private def coloredSelectedShape: List[ResizableShape with ColorizableShape] = {
    def findColorizables(xs:List[ResizableShape]): List[ResizableShape with ColorizableShape] =
      xs flatMap {
        //filter non-colrizable shapes; they have no linepattern
        case colorizable:ColorizableShape => List(colorizable)
        case g:GroupLike => findColorizables(g.childrens)
        case _ => Nil
      }

    findColorizables(getSelectedShapes)
  }

  def setFillColor(color:Color): Unit = if(!getSelectedShapes.isEmpty) {
    zippedUndo(coloredSelectedShape)(_.getFillColor)(
      _.setFillColor(color),
      _.setFillColor _
    )
  }

  def setStrokeColor(color:Color): Unit = if(!getSelectedShapes.isEmpty) {
    zippedUndo(coloredSelectedShape)(_.getStrokeColor)(
      _.setStrokeColor(color),
      _.setStrokeColor _
    )
  }

  def setStrokeWidth(width:Int): Unit = {
    zippedUndo(coloredSelectedShape)(_.getStrokeWidth)(
      _.setStrokeWidth(width),
      _.setStrokeWidth _
    )
  }

  def setStrokePattern(linePattern:LinePattern): Unit = {
    zippedUndo(coloredSelectedShape)(_.linePattern.get)(
      _.linePattern.set(linePattern),
      _.linePattern.set _
    )
//    coloredSelectedShape.foreach(x => x.linePattern.set(linePattern))
  }

  def setFillPattern(fillPattern:FillPattern): Unit = {
    zippedUndo(coloredSelectedShape)(_.fillPatternProperty.get)(
      _.fillPatternProperty.set(fillPattern),
      _.fillPatternProperty.set _
    )
  }
}
