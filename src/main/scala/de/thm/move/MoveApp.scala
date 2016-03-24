/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move

import java.util.ResourceBundle
import javafx.application.Application
import javafx.scene.text.Font
import javafx.stage.Stage
import javafx.scene.{Cursor, Parent, Scene}
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javax.annotation.Resource

import de.thm.move.controllers.MoveCtrl

class MoveApp extends Application {
  def start(stage: Stage): Unit = {
    val windowWidth = Global.config.getDouble("window.width").getOrElse(600.0)
    val windowHeight = Global.config.getDouble("window.height").getOrElse(600.0)
    val styleSheetUrl = MoveApp.getClass.getResource("/stylesheets/style.css").toExternalForm

    val fxmlLoader = new FXMLLoader(MoveApp.getClass.getResource("/fxml/move.fxml"))
    val bundle = ResourceBundle.getBundle("fonts/fontawesome")
    fxmlLoader.setResources(bundle)
    val mainViewRoot: Parent = fxmlLoader.load()
    val scene = new Scene(mainViewRoot)
    scene.getStylesheets.add(styleSheetUrl)

    stage.setTitle(Global.config.getString("window.title").getOrElse(""))
    stage.setScene(scene)
    stage.setWidth(windowWidth)
    stage.setHeight(windowHeight)
    stage.show()
    fxmlLoader.getController[MoveCtrl].setupMove()
  }
}

object MoveApp {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[MoveApp], args:_*)
  }
}
