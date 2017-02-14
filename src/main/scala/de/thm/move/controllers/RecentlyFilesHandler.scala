package de.thm.move.controllers

import java.nio.file.Path
import javafx.event.ActionEvent
import javafx.scene.control.MenuItem

import de.thm.move.implicits.FxHandlerImplicits._
import de.thm.recent._

class RecentlyFilesHandler(recent:Recent[Path], pathClicked: Path => Unit) {

  private def menuItem(path:Path): MenuItem = {
    val item = new MenuItem(path.toString)
    item.setOnAction { _:ActionEvent =>
      incrementPriorityOf(path)
      println(recent.recentValuesByPriority)
      pathClicked(path)
    }
    item
  }

  def incrementPriorityOf(path:Path): Unit =
    recent.updatePriority(path, 1) { oldVal => oldVal.priority+1 }

  def getMenuItems:Seq[MenuItem] =
    recent.recentElementsByPriority.map(menuItem)
}
