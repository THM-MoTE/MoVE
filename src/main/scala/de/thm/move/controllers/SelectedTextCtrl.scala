/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.controllers

import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment

import de.thm.move.Global._
import de.thm.move.views.shapes.ResizableText

/** Behaviour for changing selected ResizableTexts. */
trait SelectedTextCtrl {
  this: SelectionCtrlLike =>

  private def getTexts: List[ResizableText] = getSelectedShapes.flatMap {
    case x:ResizableText => List(x)
    case _ => Nil
  }

  def setFontName(name:String): Unit = {
    zippedUndo(getTexts)(
      _.getFontName)(
      _.setFontName(name),
      _.setFontName _
    )
  }
  def setFontSize(size:Int): Unit =
    zippedUndo(getTexts)(
      _.getSize)(
      _.setSize(size),
      _.setSize _
    )

  def setFontColor(c:Color): Unit =
    zippedUndo(getTexts)(
      _.getFontColor)(
      _.setFontColor(c),
      _.setFontColor _
    )

  def setFontBold(b:Boolean): Unit =
    zippedUndo(getTexts)(_ => !b)(
      _.setBold(b),
      _.setBold _
    )

  def setFontItalic(b:Boolean): Unit =
    zippedUndo(getTexts)(_ => !b)(
      _.setItalic(b),
      _.setItalic _
    )

  def setFontUnderline(b:Boolean): Unit =
    zippedUndo(getTexts)(_ => !b)(
      _.setUnderline(b),
      _.setUnderline _
    )

  def setTextAlignment(alignment:TextAlignment): Unit =
    zippedUndo(getTexts)(_.getTextAlignment)(
      _.setTextAlignment(alignment),
      _.setTextAlignment _
    )
}
