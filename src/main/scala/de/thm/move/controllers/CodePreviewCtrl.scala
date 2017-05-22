/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.controllers

import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, FXMLLoader, Initializable}
import javafx.event.ActionEvent
import javafx.scene.control.TextArea
import javafx.stage.Stage

import de.thm.move.Global
import de.thm.move.views.Windows

class CodePreviewCtrl extends Initializable {
	@FXML
	var txtCode:TextArea = _

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
		println("preview initialized")
	}

	@FXML
	def onCopy(ev:ActionEvent):Unit = {
		println("copy clicked")
	}
}

object CodePreviewCtrl {
	  def setupCodePreviewDialog(): (Stage, CodePreviewCtrl) =
			Windows.initWindow(
				Global.config.getDouble("window.preview.width").getOrElse(500.0),
				Global.config.getDouble("window.preview.height").getOrElse(500.0),
				Global.config.getDouble("window.title").map(_+" - Code Preview").getOrElse(""),
				getClass.getResource("/fxml/code-preview.fxml"),
				new CodePreviewCtrl())
}
