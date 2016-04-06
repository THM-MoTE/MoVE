/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.util

import de.thm.move.controllers.implicits.FxHandlerImplicits._
import javafx.scene.control.ChoiceBox

/** General utils for working with JavaFx. */
object JFxUtils {
  /** Adds the given listener to the selectionProperty of the given ChoiceBox.
   * The eventHandler only gets the new value and discards the old value.
   */
  def onChoiceboxChanged[A](box:ChoiceBox[A])(eventHandler: A => Unit): Unit = {
    box.getSelectionModel.
      selectedItemProperty.addListener { (_:A, newA:A) =>
        eventHandler(newA)
      }
  }
}
