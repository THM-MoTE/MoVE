/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.loader

import javafx.scene.paint.Color

import de.thm.move.models.pattern._
import de.thm.move.views.shapes._

import de.thm.move.MoveSpec
import de.thm.move.loader.parser.PropertyParser._
import de.thm.move.loader.parser.ast._
import de.thm.move.types._
import de.thm.move.util.GeometryUtils
import de.thm.move.util.GeometryUtils._


import scala.util.parsing.input.Position
import scala.util.parsing.input.NoPosition

class ConverterTest extends MoveSpec {

  "ShapeConverter.`gettCoordinateSystemSizes`" should "convert coordinate systems" in {

    val extent = ( ((-100.0),(-50.0)),((100.0),(50.0)) )
    val ast = Model("ölkj",
    Icon(Some(CoordinateSystem(extent)), List(), NoPosition, NoPosition)
    )
    val conv = new ShapeConverter(4, ShapeConverter.gettCoordinateSystemSizes(ast), null)

    val exp = (200,100)
    ShapeConverter.gettCoordinateSystemSizes(ast) shouldBe exp

    val ast2 = Model("ög",
      Icon(Some(CoordinateSystem(extent)), List(), NoPosition, NoPosition)
    )

    val x = ShapeConverter.gettCoordinateSystemSizes(ast)
    x shouldBe exp

    val extent2 = ( ((-100.0),(-50.0)),((0.0),(-20.0)) )
    val ast3 = Model("ölkj",
    Icon(Some(CoordinateSystem(extent2)), List(), NoPosition, NoPosition)
    )
    val exp2 = (100.0, 30)
    ShapeConverter.gettCoordinateSystemSizes(ast3) shouldBe exp2
  }

  "ShapeConverter" should "convert Lines" in {
    //without origin
    val extent = ( ((0.0),(0.0)),((100.0),(200.0)) )
    val ast = Model("ölkj",
    Icon(Some(CoordinateSystem(extent)),
      List(
          PathElement(
            GraphicItem(),
            List( (10.0,10.0),(50.0,30.0) ),
            Color.BLACK,
            1.0,
            "LinePattern.Dash"
            )
        ), NoPosition, NoPosition
    ))

    val conv = new ShapeConverter(1, ShapeConverter.gettCoordinateSystemSizes(ast), null)

    val convertedLine = conv.getShapes(ast).head.asInstanceOf[(ResizableLine, Option[String])]._1
    val startAnchor = convertedLine.getAnchors.head
    val endAnchor = convertedLine.getAnchors.tail.head

    startAnchor.getCenterX shouldBe 10.0
    startAnchor.getCenterY shouldBe (200.0-10.0)
    endAnchor.getCenterX shouldBe 50.0
    endAnchor.getCenterY shouldBe (200.0-30.0)
    convertedLine.getStrokeColor shouldBe Color.BLACK
    convertedLine.getStrokeWidth shouldBe 1.0
    convertedLine.linePattern.get shouldBe Dash


    val points2 = List( (10.0,10.0),(50.0,30.0), (60.0,80.0),(30.0,30.0) )
    val ast2 = Model("ölkj",
      Icon(Some(CoordinateSystem(extent)),
        List(
          PathElement(
            GraphicItem(),
            points2,
            Color.BLACK,
            1.0,
            "LinePattern.Dash"
          )
        ),NoPosition, NoPosition
      ))

    val expectedPoints = points2.map {
      case (x,y) => (x, 200-y)
    }
    val convPath = conv.getShapes(ast2).head.asInstanceOf[(ResizablePath, Option[String])]._1
    convPath.getAnchors.zip(expectedPoints).foreach {
      case (p1,p2) => p2 shouldBe (p1.getCenterX,p1.getCenterY)
    }

    val converter = new ShapeConverter(5, ShapeConverter.gettCoordinateSystemSizes(ast2), null)
    val convPath2 = converter.getShapes(ast2).head.asInstanceOf[(ResizablePath, Option[String])]._1
    convPath2.getAnchors.zip(expectedPoints.map(_.map(_*5))).foreach {
      case (p1,p2) => (p1.getCenterX,p1.getCenterY) shouldBe p2
    }
  }

  it should "convert Rectangels" in {
    val ast = Model("ölk",
      Icon(None,
        List(
          RectangleElement(GraphicItem(),
            FilledShape(),
            extent = ( (205,179),(348,36) )
          )
        ),NoPosition, NoPosition
      )
    )

    val conv = new ShapeConverter(1, ShapeConverter.gettCoordinateSystemSizes(ast), null)
    val convRec = conv.getShapes(ast).head.asInstanceOf[(ResizableRectangle, Option[String])]._1
    convRec.getXY shouldBe (205,defaultCoordinateSystemSize.y-179)
    convRec.getWidth  shouldBe 348-205
    convRec.getHeight shouldBe 179-36

    val multiplier = 5
    val conv2 = new ShapeConverter(multiplier, ShapeConverter.gettCoordinateSystemSizes(ast), null)
    val convRec2 = conv2.getShapes(ast).head.asInstanceOf[(ResizableRectangle, Option[String])]._1
    convRec2.getXY shouldBe (205*multiplier,(defaultCoordinateSystemSize.y-179)*multiplier)
    convRec2.getWidth  shouldBe ((348-205)*multiplier)
    convRec2.getHeight shouldBe ((179-36)*multiplier)
  }

  it should "convert Ellipses" in {
    val ast = Model("ölk",
      Icon(None,
        List(
          Ellipse(GraphicItem(),
            FilledShape(),
            extent = ( (205,179),(348,36) )
          )
        ),NoPosition, NoPosition
      )
    )

    val conv = new ShapeConverter(1, ShapeConverter.gettCoordinateSystemSizes(ast), null)
    val convCircle = conv.getShapes(ast).head.asInstanceOf[(ResizableCircle, Option[String])]._1
    val middleP = GeometryUtils.middleOfLine(205,
      defaultCoordinateSystemSize.y-179,
      348, defaultCoordinateSystemSize.y-36
      )
    convCircle.getWidth  shouldBe (348-205)
    convCircle.getHeight shouldBe (179-36)

    val multiplier = 2
    val conv2 = new ShapeConverter(multiplier, ShapeConverter.gettCoordinateSystemSizes(ast), null)
    val conv2Circle = conv2.getShapes(ast).head.asInstanceOf[(ResizableCircle, Option[String])]._1

    val middleP2 = GeometryUtils.middleOfLine(205*multiplier,
      (defaultCoordinateSystemSize.y-179)*multiplier,
      348*multiplier, (defaultCoordinateSystemSize.y-36)*multiplier
      )

    conv2Circle.getXY shouldBe middleP2
    conv2Circle.getWidth  shouldBe ((348-205)*multiplier)
    conv2Circle.getHeight shouldBe ((179-36)*multiplier)
  }

  it should "convert Polygons" in {
    val points = List( (205.0,179.0),(348.0,36.0),(420.0,50.0) )
    val ast = Model("ölk",
      Icon(None,
        List(
          Polygon(GraphicItem(),
            FilledShape(fillPattern = "FillPattern.Horizontal"),
            points
          )
        ),NoPosition, NoPosition
      )
    )

    val expPoints = points.map {
      case (x,y) => (x, defaultCoordinateSystemSize.y-y)
    }

    val conv = new ShapeConverter(1, ShapeConverter.gettCoordinateSystemSizes(ast), null)
    val convPolygon = conv.getShapes(ast).head.asInstanceOf[(ResizablePolygon, Option[String])]._1
    convPolygon.getAnchors.zip(expPoints).foreach {
      case (anchor,p2) =>
      val p1 = (anchor.getCenterX,anchor.getCenterY)
      p2 shouldBe p1
    }
    convPolygon.fillPatternProperty.get shouldBe Horizontal

    val multiplier = 4
    val conv2 = new ShapeConverter(multiplier, ShapeConverter.gettCoordinateSystemSizes(ast), null)
    val conv2Polygon = conv2.getShapes(ast).head.asInstanceOf[(ResizablePolygon, Option[String])]._1
    conv2Polygon.getAnchors.zip(expPoints.map(_.map(_*multiplier))).foreach {
      case (anchor,p2) =>
        val p1 = (anchor.getCenterX,anchor.getCenterY)
        p2 shouldBe p1
    }
  }

  it should "convert Images" in {
    val extent = ( (10.0,10.0),(200.0,100.0) )
    val ast =
      Model("bitmap",
        Icon(None,
          List(
            ImageURI(GraphicItem(),
              extent,
              "modelica://test3/quokka.jpg"
            )
          ),NoPosition, NoPosition
        )
      )

    //val  conv = new ShapeConverter(1, ShapeConverter.gettCoordinateSystemSizes(ast).head)
    //conv.getShapes(ast).head.asInstanceOf[ResizableImage]
  }

  it should "convert Rectangles containing a origin" in {
    val origin:Point = (10,10)
    val ast = Model("ölk",
      Icon(None,
        List(
          RectangleElement(GraphicItem(origin = origin),
            FilledShape(),
            extent = ( (-10,50),(30,-40) )
          )
        ),NoPosition, NoPosition
      )
    )

    val conv = new ShapeConverter(1, ShapeConverter.gettCoordinateSystemSizes(ast), null)
    val rect = conv.getShapes(ast).head.asInstanceOf[(ResizableRectangle, Option[String])]._1

    val expXY:Point = (10-10,defaultCoordinateSystemSize.y-(10+50))
    val expW = 10+30
    val expH = 50+40

    rect.getXY     shouldBe expXY
    rect.getWidth  shouldBe expW
    rect.getHeight shouldBe expH

    val origin2:Point = (50,30)
    val ast2 = Model("ölk",
      Icon(None,
        List(
          RectangleElement(GraphicItem(origin = origin2),
            FilledShape(),
            extent = ( (-5,70),(10,-20) )
          )
        ),NoPosition, NoPosition
      )
      )

      val multiplier = 2
      val conv2 = new ShapeConverter(multiplier, ShapeConverter.gettCoordinateSystemSizes(ast), null)

      val rect2 = conv2.getShapes(ast2).head.asInstanceOf[(ResizableRectangle, Option[String])]._1
      val expXY2:Point = (50-5,defaultCoordinateSystemSize.y-(30+70))
      val expW2 = 15
      val expH2 = 70+20

      rect2.getXY shouldBe expXY2.map(_*multiplier)
      rect2.getWidth  shouldBe (expW2*multiplier)
      rect2.getHeight shouldBe (expH2*multiplier)
  }

  it should "convert Ellipses containing a origin" in  {
    val origin:Point = (50,100)
    val ast = Model("ölk",
      Icon(None,
        List(
          Ellipse(GraphicItem(origin = origin),
            FilledShape(),
            extent = ( (-10,50),(30,-40) )
          )
        ),NoPosition, NoPosition
      )
    )

    val conv = new ShapeConverter(1, ShapeConverter.gettCoordinateSystemSizes(ast), null)
    val circ = conv.getShapes(ast).head.asInstanceOf[(ResizableCircle, Option[String])]._1

    val expCenterXY = (origin.x, defaultCoordinateSystemSize.y-origin.y)
    val expWRadius = asRadius(10+30)
    val expHRadius = asRadius(50+40)

    circ.getRadiusX shouldBe expWRadius
    circ.getRadiusY shouldBe expHRadius
    circ.getBoundsInLocal.getMinX shouldBe (40.0 +- 1)
    circ.getBoundsInLocal.getMaxX shouldBe (80.0 +- 1)
    circ.getBoundsInLocal.getMinY shouldBe ((defaultCoordinateSystemSize.y - 150) +- 1)
  }
}
