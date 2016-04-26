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

  def setSize(pt:Double): Unit = {
    val fontName = getFont.getName
    setFont(Font.font(fontName, pt))
  }

  def setFontColor(color:Paint): Unit = setFill(color)

  def setBold(flag:Boolean):Unit = {
    val familyName = getFont.getName
    val size = getSize
    val weight =
      if(flag) FontWeight.BOLD
      else FontWeight.NORMAL
    setFont(Font.font(familyName, weight, size))
    setUnderline(isUnderline)
  }

  def setItalic(flag:Boolean): Unit = {
    val familyName = getFont.getName
    val size = getSize
    val italic =
      if(flag) FontPosture.ITALIC
      else FontPosture.REGULAR
    setFont(Font.font(familyName, italic, size))
    setUnderline(isUnderline)
  }

  def getSize: Double = getFont.getSize
  def getFontColor: Paint = getFill

  /** Creates a '''exact copy''' of this element. */
  def copy: ResizableShape = ???
}
