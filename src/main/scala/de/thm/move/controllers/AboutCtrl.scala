/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.controllers

import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.TextArea
import javafx.scene.layout.{VBox, AnchorPane}

import de.thm.move.Global
import de.thm.move.views.InfoLine

class AboutCtrl extends Initializable {

  @FXML
  var aboutPaneLeft: AnchorPane = _
  @FXML
  var aboutPaneRight: VBox = _
  @FXML
  var licenseArea: TextArea = _

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
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
  }

  private def toInfoLines(m:Map[String,String]): List[InfoLine] = {
    m.map {
      case (k, v) => new InfoLine(k, v)
    }.toList
  }
}
