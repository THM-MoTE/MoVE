/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

 package de.thm.move.controllers

class ClipboardCtrl[A] {
  private var element:Option[A] = None

  def setElement(a:A):Unit = element = Some(a)
  def setElement(a:Option[A]):Unit = a foreach setElement
  def getElement:Option[A] = element
}
