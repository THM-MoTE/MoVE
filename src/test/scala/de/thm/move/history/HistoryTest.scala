/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.history

import org.junit.Assert._
import org.junit.Test
import History._
class HistoryTest {

  @Test
  def fullHistory: Unit = {
    val h = new History(10)

    var counter = 0
    val cmd = Command( () => {
      counter += 1
    }, () => {
      counter -= 1
    })

    for(_ <- 0 until 10) {
      h.execute(cmd)
    }

    assert(counter == 10)

    //nothing to redo
    for(i <- 10 until 16) {
      h.redo()
      assert(counter == 10)
    }

    //undo 4 steps
    for(i <- 10 to 0 by -1) {
      assert(counter == i)
      h.undo()
    }

    h.undo()
    assert(counter == 0)
  }

  @Test
  def testMoreThanSize: Unit = {
    val h = new History(10)

    var counter = 0
    val cmd = Command( () => {
      counter += 1
    }, () => {
      counter -= 1
    })

    for(i <- 0 until 20) {
      h.execute(cmd)
    }

    assert(counter == 20)

    for(i <- 20 until 10 by -1) {
      assert(counter==i)
      h.undo()
    }

    assert(counter==10)

    for(_ <- 0 until 5) {
      h.undo()
      assert(counter == 10)
    }

    for(i <- 10 until 21) {
      assert(counter == i)
      h.redo()
    }

    assert(counter == 20)
  }
}
