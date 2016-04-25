/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views

import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.layout.{FlowPane, GridPane}

class InfoLine(key:String, msg:String = "") extends FlowPane {
  val keyLbl = new Label(key)
  val msgLbl = new Label(msg)
  keyLbl.getStyleClass.add("key-label")
  msgLbl.getStyleClass.add("msg-label")

  if(msg.isEmpty) getChildren.add(keyLbl)
  else {
    getChildren.add(keyLbl)
    getChildren.add(msgLbl)
  }
}
