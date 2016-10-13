/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.models

import java.io.PrintWriter
import java.net.URI
import java.nio.charset.Charset
import java.nio.file.{Paths, Files}
import java.util.Base64
import javafx.scene.Node
import javafx.scene.paint.{Paint, Color}
import javafx.scene.shape.{LineTo, MoveTo}
import javafx.scene.text.TextAlignment

import de.thm.move.MoveSpec
import de.thm.move.models.CommonTypes.Point
import de.thm.move.models.ModelicaCodeGenerator.FormatSrc
import de.thm.move.models.ModelicaCodeGenerator.FormatSrc.FormatSrc
import de.thm.move.util.PointUtils._
import de.thm.move.util.ResourceUtils
import de.thm.move.util.GeometryUtils
import de.thm.move.views.shapes._

class CodeGeneratorTest extends MoveSpec {
  val dummyURL = Paths.get(System.getProperty("user.home")).toUri

  private def eqTest(toTest:String,expected:String): Unit = {
    if(!toTest.contains(expected)) {
      println(toTest)
      println("Expected: "+expected)
    }

    assert(toTest.contains(expected), s"Expected [$toTest] containing [$expected]")
  }

  "ModelicaCodeGenerator" should "generate Rectangles" in {
    val generator = new ModelicaCodeGenerator(FormatSrc.Pretty, 1, 500,500)
    val rect = new ResizableRectangle((0,0), 100,100)
    rect.colorizeShape(Color.BLACK, Color.BLACK)
    rect.setRotate(90.0)
    val str = generator.generateShape(rect, "test",  dummyURL)(1)
    eqTest(str, "origin = {50,450}")
    eqTest(str, "extent = {{-50,50}, {50,-50}}")

    val generator2 = new ModelicaCodeGenerator(FormatSrc.Pretty, 4, 500,500)
    val str2 = generator2.generateShape(rect, "test",  dummyURL)(1)
    eqTest(str2, "origin = {12,112}")
    eqTest(str2, "extent = {{-12,12}, {12,-12}}")
  }

  it should "generate Circles" in {
    val generator = new ModelicaCodeGenerator(FormatSrc.Pretty, 1, 500,500)
    val circle = new ResizableCircle((100,100), 50,50)
    circle.colorizeShape(Color.BLACK, Color.BLACK)
    circle.setRotate(90.0)
    val str = generator.generateShape(circle, "test",  dummyURL)(1)
    eqTest(str, "origin = {100,400}")
    eqTest(str, "extent = {{-50,50}, {50,-50}}")
  }
}
