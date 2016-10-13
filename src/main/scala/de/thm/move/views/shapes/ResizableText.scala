/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.views.shapes

import javafx.scene.paint.Paint
import javafx.scene.text.{Font, FontPosture, FontWeight, Text}

import de.thm.move.models.CommonTypes._
import de.thm.move.views.anchors.Anchor

class ResizableText(
    txt:String,
    x:Double,
    y:Double,
    font:Font = Font.getDefault)
  extends Text(x,y,txt)
  with ResizableShape {
  setFont(font)
  override val getAnchors: List[Anchor] = Nil

  private var isBold = false
  private var isItalic = false

  private def createNewFont(
    name:String,
    size:Double,
    isBold:Boolean,
    isItalic:Boolean): Font = {
    Font.font(name,
      if(isBold) FontWeight.BOLD else FontWeight.NORMAL,
      if(isItalic) FontPosture.ITALIC else FontPosture.REGULAR,
      size)
  }

  def setFontName(name:String):Unit = setFont(createNewFont(name, getSize, isBold, isItalic))
  def getFontName:String = getFont.getFamily

  def setSize(pt:Double): Unit = {
    setFont(createNewFont(getFontName, pt, isBold, isItalic))
  }

  def setFontColor(color:Paint): Unit = setFill(color)

  def setBold(flag:Boolean):Unit = {
    isBold = flag
    setFont(createNewFont(getFontName, getSize, isBold, isItalic))
  }

  def setItalic(flag:Boolean): Unit = {
    isItalic = flag
    setFont(createNewFont(getFontName, getSize, isBold, isItalic))
  }

  def getBold:Boolean = isBold
  def getItalic:Boolean = isItalic
  def getSize: Double = getFont.getSize
  def getFontColor: Paint = getFill
  def copy: ResizableText = {
    val txt = new ResizableText(getText, getX,getY,getFont)
    txt.setFontColor(getFontColor)
    txt.setRotate(getRotate)
    txt
  }

  override def move(delta:Point):Unit = {
    val (x,y) = delta
    setX(getX + x)
    setY(getY + y)
  }
}
