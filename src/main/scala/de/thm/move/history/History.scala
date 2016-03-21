package de.thm.move.history

import scala.collection.mutable.ArrayBuffer

class History {
  import History._

  private val undoStack = ArrayBuffer[Command]()
  private val redoStack = ArrayBuffer[Command]()

  def execute(doFn: Action)(undoFn: Action): Unit =
    execute(Command(doFn, undoFn))

  def execute(c:Command): Unit = {
    c.exec()
    undoStack.prepend(c)
  }

  def save(c:Command): Unit = undoStack.prepend(c)

  def undo(): Unit = {
    undoStack.headOption foreach { cmd =>
      undoStack.remove(0)
      redoStack.prepend(cmd)
      cmd.undo()
    }
  }

  def redo(): Unit = {
    redoStack.headOption foreach { cmd =>
      redoStack.remove(0)
      cmd.exec()
    }
  }
}

object History {
  type Action = () => Unit
  case class Command(exec: Action, undo: Action)

  val emptyAction = Command( () => Unit , () => Unit )

  def partialAction(undo: Action): Action => Command = { exec =>
    Command(exec, undo)
  }
}
