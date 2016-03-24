/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move

import java.util.ResourceBundle

import de.thm.move.history.History
import de.thm.move.config.Config
import de.thm.move.config.ConfigLoader
import de.thm.move.shortcuts.ShortCutHandler

object Global {
  lazy val shortcuts = new ShortCutHandler(getClass.getResource("/shortcuts.conf"))
  lazy val config: Config = new ConfigLoader(getClass.getResource("/move.conf"))
  lazy val history = new History()

  lazy val copyright = "(c) 2016 Nicola Justs"
  lazy val version = "0.1"

  lazy val fontBoundle = ResourceBundle.getBundle("fonts/fontawesome")
  lazy val styleSheetUrl = MoveApp.getClass.getResource("/stylesheets/style.css").toExternalForm
}
