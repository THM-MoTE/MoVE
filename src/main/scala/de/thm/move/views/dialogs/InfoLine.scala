/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.views.dialogs

import javafx.scene.control.Label
import javafx.scene.layout.FlowPane

/** Infoline used inside the about-dialog. */
class InfoLine(key:String, msg:String = "") extends FlowPane {
  val keyLbl = new Label(key)
  val msgLbl = new Label(msg)
  getStyleClass.add("infoline")
  keyLbl.getStyleClass.addAll("key-label")
  msgLbl.getStyleClass.addAll("msg-label")

  if(msg.isEmpty) getChildren.add(keyLbl)
  else {
    getChildren.add(keyLbl)
    getChildren.add(msgLbl)
  }
}
