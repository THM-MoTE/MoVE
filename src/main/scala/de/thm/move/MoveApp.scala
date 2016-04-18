/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move

import java.util.ResourceBundle
import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.control.Alert.AlertType
import javafx.scene.text.Font
import javafx.stage.{WindowEvent, Stage}
import javafx.scene.{Cursor, Parent, Scene}
import javafx.fxml.FXMLLoader
import javafx.scene.control.{Alert, Label}
import javax.annotation.Resource

import de.thm.move.controllers.MoveCtrl

class MoveApp extends Application {
  def checkExistingConfigs(): Unit = {
    try {
      Global.config.getString("window.title")
      Global.shortcuts.getKeyCode("")
    }catch {
      case _: NullPointerException =>
        val errorDialog = new Alert(AlertType.ERROR)
        errorDialog.setTitle("Loading Error")
        errorDialog.setHeaderText("Can't load configuration files!")
        errorDialog.setContentText(
          "In order to run this program you need configuration files in the conf/ directory.\n" +
          "If they don't exist this program won't load."
        )

        errorDialog.showAndWait()
        System.exit(-1)
    }
  }

  override def start(stage: Stage): Unit = {
    checkExistingConfigs()

    val windowWidth = Global.config.getDouble("window.width").getOrElse(600.0)
    val windowHeight = Global.config.getDouble("window.height").getOrElse(600.0)

    val fxmlLoader = new FXMLLoader(MoveApp.getClass.getResource("/fxml/move.fxml"))

    fxmlLoader.setResources(Global.fontBoundle)
    val mainViewRoot: Parent = fxmlLoader.load()
    val scene = new Scene(mainViewRoot)
    scene.getStylesheets.add(Global.styleSheetUrl)

    stage.setTitle(Global.config.getString("window.title").getOrElse(""))
    stage.setScene(scene)
    stage.setWidth(windowWidth)
    stage.setHeight(windowHeight)
    stage.show()
    val ctrl = fxmlLoader.getController[MoveCtrl]
    ctrl.setupMove()

    stage.setOnCloseRequest(new EventHandler[WindowEvent] {
      override def handle(event: WindowEvent): Unit = {
        ctrl.shutdownMove()
      }
    })
  }
}

object MoveApp {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[MoveApp], args:_*)
  }
}
