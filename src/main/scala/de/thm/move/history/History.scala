/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.history

import de.thm.move.Global

class History(cacheSize: Int) {
  import History._

  private var memory = List[Command]()
  private var revertedCmds = List[Command]()

  def execute(doFn: => Unit)(undoFn: => Unit): Unit =
    execute(Command( () => doFn, () => undoFn))

  def execute(c:Command): Unit = {
    c.exec()
    if(memory.length < cacheSize) memory = c :: memory
    else memory = c :: memory.init
  }

  def save(c:Command): Unit =  {
    if(memory.length < cacheSize) memory = c :: memory
    else memory = c :: memory.init
  }

  def undo(): Unit = {
    (memory headOption) foreach { x =>
      x.undo()
      memory = memory.tail
      if(revertedCmds.length < cacheSize) revertedCmds = x :: revertedCmds
      else revertedCmds = x :: revertedCmds.init
    }
  }

  def redo(): Unit = {
    (revertedCmds headOption) foreach { x =>
      x.exec()
      revertedCmds = revertedCmds.tail
      if(memory.length < cacheSize) memory = x :: memory
      else memory = x :: memory.init
    }
  }
}

object History {
  type Action = () => Unit
  case class Command(exec: Action, undo: Action)

  val emptyAction = Command( () => Unit , () => Unit )

  def newCommand(exec: => Unit, undo: => Unit):Command = Command( () => exec, () => undo )

  def partialAction(undo: => Unit)(exec: => Unit): Command = {
    Command( () => exec, () => undo)
  }

}
