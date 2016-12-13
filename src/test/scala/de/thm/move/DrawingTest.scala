/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move

import java.awt.MouseInfo
import javafx.scene.paint.Color

import com.athaydes.automaton.{FXApp, FXAutomaton, FXer}
import de.thm.move.types._
import de.thm.move.views.panes.DrawPanel
import de.thm.move.views.shapes._
import de.thm.move.util.GeometryUtils._
import de.thm.move.implicits.ConcurrentImplicits._
import org.scalactic.TolerantNumerics

class DrawingTest extends UISpec {

	def drawPanelMiddlePoint(user:FXer): Point = {
		val point = FXAutomaton.centerOf(user.getAll(classOf[DrawPanel]).get(0))
		(point.x, point.y)
	}

	"MoVE" should "draw rectangles from mouse coordinates" in {
		val user = FXer.getUserWith()
		user clickOn "rectangle_btn"

		user moveTo user.getAll(classOf[DrawPanel]).get(0)
		user dragBy (100,150)
		val recLists = user.getAll(classOf[ResizableRectangle])
		recLists.size shouldBe 1
		val rec = recLists.get(0)
		rec.getFillColor shouldBe Color.RED
		rec.getStrokeColor shouldBe Color.BLACK
		rec.getWidth shouldBe 100
		rec.getHeight shouldBe 150

		FXApp.doInFXThreadBlocking (user.getAll(classOf[DrawPanel]).get(0).remove(rec))
	}

	it should "draw circles from mouse coordinates" in {
		val user = FXer.getUserWith()
		user clickOn "circle_btn"

		user moveTo user.getAll(classOf[DrawPanel]).get(0)
		user dragBy (100,150)
		val circles = user getAll classOf[ResizableCircle]
		circles.size shouldBe 1
		val circ = circles.get(0)
		circ.getFillColor shouldBe Color.RED
		circ.getStrokeColor shouldBe Color.BLACK
		circ.getRadiusX shouldBe asRadius(100)
		circ.getRadiusY shouldBe asRadius(150)
		circ.getWidth shouldBe 100
		circ.getHeight shouldBe 150

		FXApp.doInFXThreadBlocking (user.getAll(classOf[DrawPanel]).get(0).remove(circ))
	}

	it should "draw lines from mouse coordinates" in {
		val user = FXer.getUserWith()
		user clickOn "line_btn"

		val drawPanel = user.getAll(classOf[DrawPanel]).get(0)
		user moveTo drawPanel
		val mousePoint = mousePosition
		user dragBy (100,150)
		val lines = user getAll classOf[ResizableLine]
		lines.size shouldBe 1
		val line = lines.get(0)
		line.getFillColor shouldBe Color.RED
		line.getStrokeColor shouldBe Color.BLACK
		val startP = drawPanel.localToScreen(line.getStartX, line.getStartY)
	 	startP.getX === mousePoint.x //use triple = so that coordinatePrecision is used
		startP.getY === mousePoint.y
		line.getEndX === mousePoint.x + 100
		line.getEndY === mousePoint.y + 150

		FXApp.doInFXThreadBlocking (user.getAll(classOf[DrawPanel]).get(0).remove(line))
	}

	it should "draw polygons from mouse coordinates" in {
		val user = FXer.getUserWith()
		user clickOn "polygon_btn"
		val drawPanel = user.getAll(classOf[DrawPanel]).get(0)
		user moveTo drawPanel
		val mousePoint = mousePosition
		user click()
		user moveBy (0,100)
		user click()
		user moveBy (20,-30)
		user click()
		user moveBy (0,-30)
		user click()
		user moveTo (mousePoint.x, mousePoint.y)
		user click()

		val polys = user.getAll(classOf[ResizablePolygon])
		polys.size shouldBe 1
		val poly = polys.get(0)
		poly.getFillColor shouldBe Color.RED
		poly.getStrokeColor shouldBe Color.BLACK

		val lines = List(mousePoint,
			mousePoint + (0,100),
			mousePoint + (0,100) + (20,-30),
			mousePoint + (0,100) + (20,-30) + (0,-30)
		)
		val edges = for(i <- 0 until poly.edgeCount) yield poly.getEdgePoint(i)
		for {
			(p1,p2) <- edges zip lines
		} {
			val scrP = poly.localToScreen(p1.x,p2.y)
			scrP.getX === p2.x
			scrP.getY === p2.y
		}
	}
}
