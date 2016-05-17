/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.shortcuts
import java.net.URL
import javafx.scene.input.{KeyCode, KeyCombination}

import de.thm.move.config.ConfigLoader

class ShortCutHandler(src:URL) {
  private val shortcutConfig = new ConfigLoader(src)

  def getShortcut(key:String):Option[KeyCombination] =
    shortcutConfig.getString(key).map(KeyCombination.valueOf)

  def getKeyCode(key:String): Option[KeyCode] =
    shortcutConfig.getString(key).map(_.toUpperCase).map(KeyCode.valueOf)
}
