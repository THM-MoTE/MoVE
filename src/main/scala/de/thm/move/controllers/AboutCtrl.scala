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
import javafx.scene.control.TextArea
import javafx.scene.layout.{AnchorPane, VBox}
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

import de.thm.move.Global
import de.thm.move.views.Windows
import de.thm.move.views.dialogs.InfoLine

class AboutCtrl extends Initializable {

  @FXML
  var aboutPaneLeft: AnchorPane = _
  @FXML
  var aboutPaneRight: VBox = _
  @FXML
  var licenseArea: TextArea = _

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    //about pane
    val m = Map(
      "Copyright" -> Global.copyright,
      "Version" -> Global.version
    )

    val elementOpts = List(
    )
    val elements = elementOpts.flatten
    val infolines = elements.map(new InfoLine(_))

    Global.config.getString("window.title").
      map(new InfoLine(_)).
      foreach(aboutPaneRight.getChildren.add)

    aboutPaneRight.getChildren.addAll(toInfoLines(m):_*)
    aboutPaneRight.getChildren.addAll(infolines:_*)

    //setup license pane
    licenseArea.setText(Global.licenseString)
  }

  private def toInfoLines(m:Map[String,String]): List[InfoLine] = {
    m.map {
      case (k, v) => new InfoLine(k, v)
    }.toList
  }
}

object AboutCtrl {
  def setupAboutDialog(): (Stage, AboutCtrl) =
    Windows.initWindow(
      Global.config.getDouble("window.about.width").getOrElse(500.0),
      Global.config.getDouble("window.about.height").getOrElse(500.0),
      Global.config.getString("window.title").map(_+" - About").getOrElse(""),
      getClass.getResource("/fxml/about.fxml"),
      new AboutCtrl())
}
