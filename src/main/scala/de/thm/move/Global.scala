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
  lazy val version = "0.5"

  lazy val licenseFile = "/LICENSE"

  lazy val fontBoundle = ResourceBundle.getBundle("fonts/fontawesome")
  lazy val styleSheetUrl = MoveApp.getClass.getResource("/stylesheets/style.css").toExternalForm

  lazy val licenseString =
    Option( getClass.getResource(licenseFile) ).map { file =>
      scala.io.Source.fromURL(file,  "UTF-8").getLines.mkString("\n")
    }.getOrElse("Can't load license!")

  lazy val minScaleFactor = 1
  lazy val maxScaleFactor = 100


  /** Helper function for implementing undo-/redo on a list of elements of the same type.
    *
    * First map the elements of xs with function fn.
    * Then applys for each element in xs exec() in redo.
    * Last apply for each zipped element (A,B) undo in undo.
    *
    * @param xs the target list of elements
    * @param fn the function with which to map the elements inside xs
    * @param exec the function for redo this action
    * @param undo the function for undo this action
    */
  def zippedUndo[A, B](xs:List[A])(
                              fn: A => B)(
                              exec: A => Unit,
                              undo: A => B => Unit): Unit = {
    val zipped = xs zip xs.map(fn)
    history.execute {
      xs.foreach(exec)
    } {
      zipped.foreach {
        case (a,b) => undo(a)(b)
      }
    }
  }
}
