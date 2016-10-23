/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.views

import javafx.scene.control.{ContextMenu, MenuItem, Label}

class ShapeContextMenu extends ContextMenu {
  private def withFontIcon(elem:MenuItem, iconIdent:String): MenuItem = {
    val lbl = new Label(iconIdent)
    lbl.getStyleClass().add("toolbar-button")
    elem.setGraphic(lbl)
    elem
  }

  val inForegroundItem = new MenuItem("In foreground")
  val inBackgroundItem = new MenuItem("In background")
  val duplicateElementItem = withFontIcon(new MenuItem("Duplicate"), "\uf24d")
  val resetRotationElementItem = new MenuItem("Reset rotation")
  val rotate90ClockwiseItem = withFontIcon(new MenuItem("Rotate 90° clockwise"), "\uf01e")
  val rotate90CounterClockwiseItem = withFontIcon(new MenuItem("Rotate 90° counter-clockwise"), "\uf0e2")
  val rotate45ClockwiseItem = withFontIcon(new MenuItem("Rotate 45 clockwise"), "\uf01e")
  val rotate45CounterClockwiseItem = withFontIcon(new MenuItem("Rotate 45° counter-clockwise"), "\uf0e2")

  getItems.addAll(
    inForegroundItem,
    inBackgroundItem,
    duplicateElementItem,
    resetRotationElementItem,
    rotate90ClockwiseItem,
    rotate90CounterClockwiseItem,
    rotate45ClockwiseItem,
    rotate45CounterClockwiseItem)
}
