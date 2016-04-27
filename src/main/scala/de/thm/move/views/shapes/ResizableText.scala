/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views.shapes

import javafx.scene.paint.Paint
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import de.thm.move.views.Anchor
import de.thm.move.util.PointUtils._
import de.thm.move.models.CommonTypes._

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
  def getFontName:String = getFont.getName

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

  /** Creates a '''exact copy''' of this element. */
  def copy: ResizableShape = ???
}
