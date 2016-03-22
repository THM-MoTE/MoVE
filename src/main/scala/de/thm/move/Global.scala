package de.thm.move

import de.thm.move.history.History
import de.thm.move.config.Config
import de.thm.move.config.ConfigLoader

object Global {
  
  lazy val config: Config = new ConfigLoader(getClass.getResource("/move.conf"))
  lazy val history = new History()
}
