/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move

import java.net.URL
import java.util.ResourceBundle

import de.thm.move.history.History
import de.thm.move.config.Config
import de.thm.move.config.ConfigLoader
import de.thm.move.shortcuts.ShortCutHandler

object Global {
  lazy val fillColorConfigURI:URL = getClass.getResource("/fillColor.conf")
  lazy val strokeColorConfigURI:URL = getClass.getResource("/strokeColor.conf")

  lazy val shortcuts = new ShortCutHandler(getClass.getResource("/shortcuts.conf"))
  lazy val config: Config = new ConfigLoader(getClass.getResource("/move.conf"))

  lazy val historySize = Global.config.getInt("history.cache-size").getOrElse(50)
  lazy val history = new History(historySize)

  lazy val copyright = "(c) 2016 Nicola Justs"
  lazy val version = "0.1"

  lazy val licenseFile = "/LICENSE"

  lazy val fontBoundle = ResourceBundle.getBundle("fonts/fontawesome")
  lazy val styleSheetUrl = MoveApp.getClass.getResource("/stylesheets/style.css").toExternalForm

  lazy val licenseString =
    Option( getClass.getResource(licenseFile) ).map { file =>
      scala.io.Source.fromURL(file,  "UTF-8").getLines.mkString("\n")
    }.getOrElse("Can't load license!")

  lazy val minScaleFactor = 1
  lazy val maxScaleFactor = 100
}
