/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.history

class History(cacheSize: Int) {
  import History._

  /*
    Commands are moved between these 2 lists when someone uses un-/redo
  */
  private var memory = List[Command]() //all executed cmds
  private var revertedCmds = List[Command]() //all reverted cmds

  def execute(doFn: => Unit)(undoFn: => Unit): Unit =
    execute(Command( () => doFn, () => undoFn))

  def execute(c:Command): Unit = {
    c.exec()
    save(c)
  }

  def save(c:Command): Unit =  {
    memory = addWithFixedSize(memory, c, cacheSize)
  }

  def undo(): Unit = {
    memory.headOption foreach { x =>
      x.undo()
      memory = memory.tail
      revertedCmds = addWithFixedSize(revertedCmds, x, cacheSize)
    }
  }

  def redo(): Unit = {
    revertedCmds.headOption foreach { x =>
      x.exec()
      revertedCmds = revertedCmds.tail
      memory = addWithFixedSize(memory, x, cacheSize)
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


  /** Prepends the element to the list if xs.size < fixedSize,
    * if xs.size>=fixedSize the last element of the list is dropped
    * for this new element!
    */
  private[history]
  def addWithFixedSize[A](xs:List[A], elem:A, fixedSize:Int): List[A] = {
    if(xs.length < fixedSize) elem :: xs
    else elem :: xs.init
  }
}
