/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
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

import org.junit.Assert._
import org.junit.Test

import de.thm.move.models.CommonTypes.Point
import de.thm.move.models.ModelicaCodeGenerator.FormatSrc
import de.thm.move.models.ModelicaCodeGenerator.FormatSrc.FormatSrc
import de.thm.move.util.PointUtils._
import de.thm.move.util.ResourceUtils
import de.thm.move.util.GeometryUtils
import de.thm.move.views.shapes._

class CodeGeneratorTest {
  val dummyURL = Paths.get(System.getProperty("user.home")).toUri

  private def eqTest(toTest:String,expected:String): Unit = {
    if(!toTest.contains(expected)) {
      println(toTest)
      println("Expected: "+expected)
    }

    assertTrue(toTest.contains(expected))
  }

  @Test
  def rectangleTest: Unit = {
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

  @Test
  def circleTest: Unit = {
    val generator = new ModelicaCodeGenerator(FormatSrc.Pretty, 1, 500,500)
    val circle = new ResizableCircle((100,100), 50,50)
    circle.colorizeShape(Color.BLACK, Color.BLACK)
    circle.setRotate(90.0)
    val str = generator.generateShape(circle, "test",  dummyURL)(1)
    eqTest(str, "origin = {100,400}")
    eqTest(str, "extent = {{-50,50}, {50,-50}}")
  }
}
