package de.thm.move

import javafx.application.Application
import javafx.stage.Stage
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label

class MoveApp extends Application {
  def start(stage: Stage): Unit = {    
    val fxmlLoader = new FXMLLoader(MoveApp.getClass.getResource("/de/thm/move/views/move.fxml"))
    val mainViewRoot: Parent = fxmlLoader.load()   
    val scene = new Scene(mainViewRoot);
    
    stage.setTitle("Move")
    stage.setScene(scene)
    stage.setWidth(600)
    stage.setHeight(600)
    stage.show()
  }
}

object MoveApp {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[MoveApp], args:_*)
  }
}