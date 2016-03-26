/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move

import de.thm.move.history.History
import de.thm.move.config.Config
import de.thm.move.config.ConfigLoader
import de.thm.move.shortcuts.ShortCutHandler

object Global {
  lazy val shortcuts = new ShortCutHandler(getClass.getResource("/shortcuts.conf"))
  lazy val config: Config = new ConfigLoader(getClass.getResource("/move.conf"))
  lazy val historySize = Global.config.getInt("history.cache-size").getOrElse(50)
  lazy val history = new History(historySize)
}
