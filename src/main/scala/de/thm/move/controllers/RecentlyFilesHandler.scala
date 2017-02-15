package de.thm.move.controllers

import java.nio.file.{Files, Path, StandardOpenOption}
import javafx.event.ActionEvent
import javafx.scene.control.MenuItem

import de.thm.move.Global
import de.thm.move.implicits.FxHandlerImplicits._
import de.thm.move.util.JFxUtils
import de.thm.recent._
import spray.json.JsonFormat

class RecentlyFilesHandler(recent:Recent[Path], pathClicked: Path => Unit) {

  private def menuItem(path:Path): MenuItem = {
    val item = new MenuItem(path.toString)
    JFxUtils.addFontIcon(item, "\uf1c9")
    item.setOnAction { _:ActionEvent =>
      incrementPriorityOf(path)
      println(recent.recentValuesByPriority)
      pathClicked(path)
    }
    item
  }

  def incrementPriorityOf(path:Path): Unit =
    recent.incrementPriority(path)

  def getMenuItems:Seq[MenuItem] =
    recent.recentElementsByPriority.map(menuItem)

  def writeTo(outputFile:Path)(implicit pathFormat:JsonFormat[Path]): Unit = {
    val jsonString = recent.toJson
    Files.write(outputFile, jsonString.getBytes(Global.encoding))
  }
}
