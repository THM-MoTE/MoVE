/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.views
import java.net.URL
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

import de.thm.move.Global

object Windows {
	def initWindow[Ctrl](width:Double,
		height:Double,
		title:String,
		view:URL,
		ctrl:Ctrl):(Stage, Ctrl) = {
			val stage = new Stage()
			val fxmlLoader = new FXMLLoader(view)
			fxmlLoader.setResources(Global.fontBundle)
	    fxmlLoader.setController(ctrl)
	    val root:Parent = fxmlLoader.load()
			val scene = new Scene(root)
			scene.getStylesheets.add(Global.styleSheetUrl)
			stage.setTitle(title)
			stage.setScene(scene)
			stage.setWidth(width)
			stage.setHeight(height)
			(stage, ctrl)
	}
}
