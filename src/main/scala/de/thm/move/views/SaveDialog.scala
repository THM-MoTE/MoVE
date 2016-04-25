/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views

import javafx.scene.control.{ButtonType, Alert}
import javafx.scene.control.Alert.AlertType

class SaveDialog extends Alert(AlertType.CONFIRMATION) {
  val onelineBtn = new ButtonType("One liner")
  val prettyBtn = new ButtonType("Pretty")

  setTitle("Modelica Sourcecode")
  setHeaderText("Pretty formatted code or one-line code?")
  getButtonTypes.addAll(onelineBtn, prettyBtn)
}
