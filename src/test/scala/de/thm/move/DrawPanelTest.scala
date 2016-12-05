/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move

import de.thm.move.views.SelectionGroup
import de.thm.move.views.anchors.Anchor
import de.thm.move.views.panes.DrawPanel
import de.thm.move.views.shapes._
import javafx.scene.layout.Pane

class DrawPanelTest extends MoveSpec {

	"A DrawPanel" should  "have a fixed size" in {
		val panel = new DrawPanel()
		panel.setSize(400,400)

		panel.getMinWidth() shouldBe 400
		panel.getMinHeight() shouldBe 400
		panel.getPrefWidth() shouldBe 400
		panel.getPrefHeight() shouldBe 400
		panel.getMaxWidth() shouldBe 400
		panel.getMaxHeight() shouldBe 400
	}

	it should "return only ResizableShapes" in {
		val panel = new DrawPanel()
		panel.setSize(400,400)
		val selGroup = new SelectionGroup(List())
		val rec = new ResizableRectangle((0,0), 50,50)
		val rec2 = new ResizableRectangle((100,100), 150,200)
		val circ = new ResizableCircle((200,150), 100,100)
		val anchor = new Anchor(20,5)
		val anchor2 = new Anchor(20,5)
		panel.getChildren().addAll(rec,rec2,selGroup, anchor,anchor2,circ)
		panel.getShapes shouldBe List(rec,rec2,circ)
	}
}
