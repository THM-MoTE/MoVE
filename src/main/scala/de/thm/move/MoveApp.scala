package de.thm.move

import java.util.ResourceBundle
import javafx.application.Application
import javafx.scene.text.Font
import javafx.stage.Stage
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javax.annotation.Resource

class MoveApp extends Application {
  def start(stage: Stage): Unit = {
    val styleSheetUrl = MoveApp.getClass.getResource("/stylesheets/style.css").toExternalForm

    val fxmlLoader = new FXMLLoader(MoveApp.getClass.getResource("/fxml/move.fxml"))
    val bundle = ResourceBundle.getBundle("fonts/fontawesome")
    fxmlLoader.setResources(bundle)
    val mainViewRoot: Parent = fxmlLoader.load()   
    val scene = new Scene(mainViewRoot)
    scene.getStylesheets.add(styleSheetUrl)

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