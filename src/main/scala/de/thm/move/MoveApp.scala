/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move

import java.nio.file.{ Files, Paths }
import javafx.application.Application
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.{Parent, Scene}
import javafx.stage.{Stage, WindowEvent}

import de.thm.move.controllers.MoveCtrl
import scala.collection.JavaConverters._

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
    val parameters = getParameters.getRaw.asScala

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
    ctrl.setupMove(stage, parameters.headOption)

    stage.setOnCloseRequest(new EventHandler[WindowEvent] {
      override def handle(event: WindowEvent): Unit = {
        ctrl.shutdownMove()
      }
    })
  }
}

object MoveApp {
  val name = build.ProjectInfo.name
  val copyright = build.ProjectInfo.copyright
  val version = build.ProjectInfo.version
  val licenseName = build.ProjectInfo.licenseName

  def printVersion(): Unit = {
    val versionInfo = s"""$name - V$version - $copyright - $licenseName"""
    println(versionInfo)
  }

  def help(): Unit = {
    val helpMsg =
      s"""Usage:
      |\tjava -jar $name-$version.jar
      |\tjava -jar $name-$version.jar [filename]
      |
      |Options:
      |\t-help\t\tDisplay this help message
      |\t-version\tPrint version & exit""".stripMargin
    println(helpMsg)
  }

  def main(args: Array[String]): Unit = {
    args.toList match {
      case "-help" :: _ => help()
      case "-version" :: _ => printVersion()
      case path :: _ if !Files.exists(Paths.get(path)) || !Files.isRegularFile(Paths.get(path)) =>
        println(s"WARNING: The given file [$path] wasn't a file or doesn't exist!")
        Application.launch(classOf[MoveApp])
      case path :: _ => Application.launch(classOf[MoveApp],path)
      case _ => Application.launch(classOf[MoveApp])
    }
  }
}
