package de.thm.move.views

import javafx.scene.control.{ButtonType, Alert}
import javafx.scene.control.Alert.AlertType

class SaveDialog extends Alert(AlertType.CONFIRMATION) {
  setTitle("Modelica Sourcecode")
  setHeaderText("Pretty formatted code or one-line code?")

  val onelineBtn = new ButtonType("One liner")
  val prettyBtn = new ButtonType("Pretty")
  getButtonTypes.addAll(onelineBtn, prettyBtn)
}
