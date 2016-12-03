/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move

import javafx.scene.layout.Pane
import javafx.scene.shape.Line
import de.thm.move.views.panes.SnapGrid
import scala.collection.JavaConverters._

class SnapGridTest extends MoveSpec {

	"A SnapGrid" should  "have the same size as its `topPane`" in {
		val pane = new Pane()
		val (w,h) = (500, 500)
		pane.setPrefWidth(w)
		pane.setPrefHeight(h)
		val snapGrid = new SnapGrid(pane, 50, 5)
		snapGrid.getPrefWidth() shouldBe w
		snapGrid.getPrefHeight() shouldBe h
		snapGrid.getMaxWidth() shouldBe pane.getMaxWidth
		snapGrid.getMaxHeight() shouldBe pane.getMaxHeight
	}

	it should "only contain lines" in {
		val pane = new Pane()
		val (w,h) = (800,500)
		val cellSize = 50
		pane.setPrefWidth(w)
		pane.setPrefHeight(h)
		val snapGrid = new SnapGrid(pane, cellSize, 5)
		val childrens = snapGrid.getChildren().asScala
		childrens.forall(_.isInstanceOf[Line])
	}

	it should "contain $(width/cellSize) of vertical lines" in {
		//TODO use a jfx test framework in order to actually run the application,
		//the width/height calculation, which is needed for the grid generation is triggered during
		//the layout process of the parent component. There is no parent component if there is no
		//jfx application!
		val pane = new Pane()
		val (w,h) = (800,500)
		val cellSize = 50
		pane.setPrefWidth(w)
		pane.setPrefHeight(h)
		val snapGrid = new SnapGrid(pane, cellSize, 5)
		snapGrid.gridVisibleProperty.set(true)
		val childrens = snapGrid.getChildren().asScala
		val vertLines = childrens.filter(_.getId==snapGrid.verticalLineId)
		vertLines.size shouldBe (w/cellSize)
		vertLines.forall(_.isInstanceOf[Line])
	}

	it should "contain $(height/cellSize) of horizontal lines" in {
		val pane = new Pane()
		val (w,h) = (500,800)
		val cellSize = 50
		pane.setPrefWidth(w)
		pane.setPrefHeight(h)
		val snapGrid = new SnapGrid(pane, cellSize, 5)
		snapGrid.gridVisibleProperty.set(false)
		snapGrid.gridVisibleProperty.set(true)
		val childrens = snapGrid.getChildren().asScala
		val horizLines = childrens.filter(_.getId==snapGrid.horizontalLineId)
		horizLines.size shouldBe (h/cellSize)
		horizLines.forall(_.isInstanceOf[Line])
	}

	it should "contain lines that are as tall as the pane" in {
		val pane = new Pane()
		val (w,h) = (500,800)
		val cellSize = 50
		pane.setPrefWidth(w)
		pane.setPrefHeight(h)
		val snapGrid = new SnapGrid(pane, cellSize, 5)
		snapGrid.gridVisibleProperty.set(true)
		val childrens = snapGrid.getChildren().asScala
		val vertLines = childrens.filter(_.getId==snapGrid.verticalLineId)
		vertLines.map(_.asInstanceOf[Line]).forall { line =>
			line.getStartY == 0 &&
			line.getEndY == h
		}
	}

	it should "contain lines that are as wide as the pane" in {
		val pane = new Pane()
		val (w,h) = (500,800)
		val cellSize = 50
		pane.setPrefWidth(w)
		pane.setPrefHeight(h)
		val snapGrid = new SnapGrid(pane, cellSize, 5)
		snapGrid.gridVisibleProperty.set(true)
		val childrens = snapGrid.getChildren().asScala
		val horizLines = childrens.filter(_.getId==snapGrid.horizontalLineId)
		horizLines.map(_.asInstanceOf[Line]).forall { line =>
			line.getStartX == 0 &&
			line.getEndX == w
		}
	}
}
