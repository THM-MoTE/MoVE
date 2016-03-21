package de.thm.move.history

import scala.collection.mutable.ArrayBuffer

class History {

  type Action = () => Unit
  type UndoAction = () => Unit

  private val undoStack = ArrayBuffer[Command]()
  private val redoStack = ArrayBuffer[Command]()

  def execute(doFn: Action)(undoFn: Action): Unit =
    execute(Command(doFn, undoFn))

  def execute(c:Command): Unit = {
    c.exec
    undoStack += c
  }

  def undo: Unit = {
    undoStack.headOption foreach { cmd =>
      undoStack.remove(0)
      cmd.undo
    }
  }

  def redo: Unit = {
    redoStack.headOption foreach { cmd =>
      redoStack.remove(0)
      cmd.exec
    }
  }

  case class Command(exec: Action, undo:Action)
}
