/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.controllers

import java.util.ResourceBundle
import java.net.URL
import javafx.event.ActionEvent
import javafx.scene.control._
import javafx.fxml.{FXML, FXMLLoader, Initializable}
import implicits.FxHandlerImplicits._

class TextToolbarCtrl extends Initializable {

  @FXML var fontColorLbl:Label = _
  @FXML var fontColorChooser:ColorPicker = _

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    fontColorChooser.setOnAction { _:ActionEvent =>
      fontColorLbl.setTextFill(fontColorChooser.getValue)
    }
  }
}
