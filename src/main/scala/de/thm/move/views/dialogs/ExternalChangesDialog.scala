/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.views.dialogs

import de.thm.move.Global
import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.control.ButtonBar.ButtonData

class ExternalChangesDialog(filename:String) extends Alert(AlertType.CONFIRMATION) {
  val overwriteAnnotationsBtn = new ButtonType(Global.fontBundle.getString("filechanges.overwrite-btn"))
  val cancelBtn = new ButtonType(Global.fontBundle.getString("cancel-btn"), ButtonData.CANCEL_CLOSE)

  setTitle(Global.fontBundle.getString("filechanges.title"))
  setHeaderText(Global.fontBundle.getString("filechanges.header").replace("{x}", filename))
  setContentText(Global.fontBundle.getString("filechanges.content"))
  getButtonTypes().setAll(overwriteAnnotationsBtn, cancelBtn)
 }
