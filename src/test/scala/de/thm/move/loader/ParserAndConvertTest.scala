/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.loader

import javafx.scene.paint.Color

import de.thm.move.MoveSpec
import de.thm.move.loader.parser.PropertyParser._
import de.thm.move.views.shapes._
import de.thm.move.util.PointUtils._
import parser._

class ParserAndConvertTest extends MoveSpec {

  "Parsing and Converting" should "parse & convert Rectangles" in {
    val str =
    """
      |model test3
      |      annotation(
      |      Icon (
      |        coordinateSystem(
      |          extent = {{0,0},{500,500}}
      |        ),
      |        graphics = {
      |Rectangle(
      | extent = { {0,0},{50,50} },
      | lineColor = {0,0,0},
      |            fillColor = {255,0,0},
      |            lineThickness = 2.0,
      |            pattern = LinePattern.Solid,
      |            fillPattern = FillPattern.Solid
      |)}));
      |end test3;
    """.stripMargin

    val parsed = withParseSuccess(str)
    val conv = new ShapeConverter(1, ShapeConverter.gettCoordinateSystemSizes(parsed), null)
    val convertedRectangle = conv.getShapes(parsed).head.asInstanceOf[(ResizableRectangle, Option[String])]._1

    convertedRectangle.getXY shouldBe (0,(500))
    convertedRectangle.getWidth  shouldBe (50)
    convertedRectangle.getHeight shouldBe (50)


    val multiplier = 4
    val conv2 = new ShapeConverter(multiplier, ShapeConverter.gettCoordinateSystemSizes(parsed), null)
    val convertedRectangle2 = conv2.getShapes(parsed).head.asInstanceOf[(ResizableRectangle, Option[String])]._1

    convertedRectangle2.getXY shouldBe (0,500*multiplier)
    convertedRectangle2.getWidth  shouldBe 50*multiplier
    convertedRectangle2.getHeight shouldBe 50*multiplier
    convertedRectangle2.getStrokeWidth shouldBe 2.0
    convertedRectangle2.getFillColor   shouldBe Color.RED
    convertedRectangle2.getStrokeColor shouldBe Color.BLACK

    val str2 =
      """
        |model test3
        |      annotation(
        |      Icon (
        |        coordinateSystem(
        |          extent = {{0,0},{500,500}}
        |        ),
        |        graphics = {
        |Rectangle(
        | extent = { {20,60},{100,150} },
        | lineColor = {0,0,0},
        |            fillColor = {255,0,0},
        |            lineThickness = 2.0,
        |            pattern = LinePattern.Solid,
        |            fillPattern = FillPattern.Solid
        |)}));
        |end test3;
      """.stripMargin

    val parsed2 = withParseSuccess(str2)
    val conv3 = new ShapeConverter(1, ShapeConverter.gettCoordinateSystemSizes(parsed), null)
    val convertedRectangle3 = conv.getShapes(parsed2).head.asInstanceOf[(ResizableRectangle, Option[String])]._1

    convertedRectangle3.getXY shouldBe (20,500-60)
    convertedRectangle3.getWidth  shouldBe (100-20)
    convertedRectangle3.getHeight shouldBe (150-60)
  }
}
