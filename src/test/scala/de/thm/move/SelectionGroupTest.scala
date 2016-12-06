/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move

import de.thm.move.views.SelectionGroup
import de.thm.move.views.shapes._

class SelectionGroupTest extends MoveSpec {
	"A SelectionGroup" should "move al of its components" in {
		val rec = new ResizableRectangle((100,100), 50,50)
		val rec2 = new ResizableRectangle((100,100), 150,200)
		val circ = new ResizableCircle((100,100), 100,100)
		val selGroup = new SelectionGroup(List(rec,rec2,circ))
		selGroup.move((20,10))
		selGroup.childrens.foreach { case shape:RectangleLike =>
			shape.getXY shouldBe (120.0 -> 110.0)
		}

		selGroup.move((-30,-30))
		selGroup.childrens.foreach { case shape:RectangleLike =>
			shape.getXY shouldBe (90.0 -> 80.0)
		}
	}

	it should "deep copy its components" in {
		val rec = new ResizableRectangle((100,100), 50,50)
		val rec2 = new ResizableRectangle((100,100), 150,200)
		val circ = new ResizableCircle((100,100), 100,100)
		val selGroup = new SelectionGroup(List(rec,rec2,circ))
		val newGroup = selGroup.copy.asInstanceOf[SelectionGroup]
		newGroup should not be theSameInstanceAs (selGroup)
		newGroup.childrens.foreach { shape =>
			all(selGroup.childrens) should not be theSameInstanceAs (shape)
		}
		//use string representation for equality test. this is only for simplicity
		newGroup.childrens.map(_.toString) shouldBe selGroup.childrens.map(_.toString)
	}
	it should "return its components' anchors" in {
		val rec = new ResizableRectangle((100,100), 50,50)
		val rec2 = new ResizableRectangle((100,100), 150,200)
		val circ = new ResizableCircle((100,100), 100,100)
		val selGroup = new SelectionGroup(List(rec,rec2,circ))
		selGroup.getAnchors shouldBe (rec.getAnchors ++ rec2.getAnchors ++ circ.getAnchors)
	}
}
