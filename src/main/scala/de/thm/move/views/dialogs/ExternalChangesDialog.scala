/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.views.dialogs

import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.control.ButtonBar.ButtonData

class ExternalChangesDialog(filename:String) extends Alert(AlertType.CONFIRMATION) {
  val overwriteAnnotationsBtn = new ButtonType("Reparse file and overwrite annotations")
  val cancelBtn = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE)

  setTitle("External file changes!")
  setHeaderText(s"Another program changed the file ${filename}!")
  setContentText("What do you want to do?")
  getButtonTypes().setAll(overwriteAnnotationsBtn, cancelBtn)
 }
