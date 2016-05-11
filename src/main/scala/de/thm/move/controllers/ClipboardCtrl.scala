/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

 package de.thm.move.controllers

/** A controller for a clipboard.
  * A clipboard contains optonally an element of type A
  * (If no one added an element the clipboard is empty ==> getElement returns None)
  */
class ClipboardCtrl[A] {
  private var element:Option[A] = None

  def setElement(a:A):Unit = element = Some(a)
  def setElement(a:Option[A]):Unit = a foreach setElement
  def getElement:Option[A] = element
}
