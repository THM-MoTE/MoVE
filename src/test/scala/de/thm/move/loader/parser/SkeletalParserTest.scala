/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.loader.parser

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import javafx.scene.paint.Color

import scala.util.parsing.input.Position
import scala.util.parsing.input.NoPosition

import de.thm.move.MoveSpec
import de.thm.move.loader.parser.PropertyParser._
import de.thm.move.loader.parser.ast._

class SkeletalParserTest extends MoveSpec {

  "SkeletalParser" should "parse rectangles" in {
    val rect =
      """
        |model abc
        | annotation(
        |   Icon( graphics = {
        |     Rectangle(
        |        pattern = LinePattern.Solid,
        |        fillPattern = FillPattern.HorizontalCylinder,
        |        lineColor = {0,0,0},
        |        fillColor = {255,0,0},
        |        extent = {{51,471}, {400,299}}
        |      )
        |   })
        |   );
        |end abc;
      """.stripMargin
    val erg = withParseSuccess(rect)
  }

  it should "parse basic icons" in {
    val modelTest =
      """
        |model test
        | annotation(
        |   Icon (
        |     graphics = {}
        |   )
        | );
        |end test;
      """.stripMargin

    val modelTestErg = withParseSuccess(modelTest)
    val modelTestExpec =
      Model("test",
          Icon(None, Nil, NoPosition, NoPosition)
        )
    iconEqual(modelTestExpec, modelTestErg)


    val rect =
      """
        |model abc
        | annotation(
        |   Icon( graphics = {
        |     Rectangle(
        |        lineColor = {0,0,0},
        |        fillColor = {255,0,0},
        |        lineThickness = 4.0,
        |        pattern = LinePattern.Solid,
        |        fillPattern = FillPattern.HorizontalCylinder,
        |        extent = {{51,471}, {400,299}}
        |      )
        |   })
      |   );
        |end abc;
      """.stripMargin
    val erg = withParseSuccess(rect)

    val expectedRect =
      RectangleElement(
        GraphicItem(defaultVisible, defaultOrigin, 0.0),
        FilledShape(
          Color.RED,
          "FillPattern.HorizontalCylinder",
          Color.BLACK,
          4.0,
          "LinePattern.Solid"
        ),
        "BorderPattern.None",
        ( (51,471),(400,299) )
      )

    val rectExp =
      Model("abc",
          Icon(
            None,
            List(
              expectedRect
            ), NoPosition, NoPosition
          )
        )

    iconEqual(rectExp, erg)


    val rectRect =
      """
        |model abc
        | annotation(
        |   Icon( graphics = {
        |     Rectangle(
        |        lineColor = {0,0,0},
        |        fillColor = {255,0,0},
        |        lineThickness = 4.0,
        |        pattern = LinePattern.Solid,
        |        fillPattern = FillPattern.HorizontalCylinder,
        |        extent = {{51,471}, {400,299}}
        |      ),
        |      Rectangle(
        |        lineColor = {0,0,0},
        |        fillColor = {255,0,0},
        |        lineThickness = 4.0,
        |        pattern = LinePattern.Solid,
        |        fillPattern = FillPattern.HorizontalCylinder,
        |        extent = {{51,471}, {400,299}}
        |      ),
        |      Rectangle(
        |        lineColor = {0,0,0},
        |        fillColor = {255,0,0},
        |        lineThickness = 4.0,
        |        pattern = LinePattern.DashDot,
        |        fillPattern = FillPattern.HorizontalCylinder,
        |        extent = {{51,471}, {400,299}}
        |      ),
        |      Rectangle(
        |        lineColor = {0,0,0},
        |        fillColor = {255,0,0},
        |        lineThickness = 4.0,
        |        pattern = LinePattern.Dash,
        |        fillPattern = FillPattern.HorizontalCylinder,
        |        extent = {{51,471}, {400,299}}
        |      )
        |   })
        |   );
        |end abc;
      """.stripMargin

    val erg2 = withParseSuccess(rectRect)
    val rectangles = List.fill(4)(expectedRect)
    val expErg2 =
      Model("abc",
          Icon(
            None,
            List(RectangleElement(
              GraphicItem(defaultVisible, defaultOrigin, 0.0),
              FilledShape(
                Color.RED,
                "FillPattern.HorizontalCylinder",
                Color.BLACK,
                4.0,
                "LinePattern.Solid"
              ),
              "BorderPattern.None",
              ( (51,471),(400,299) )
            ),RectangleElement(
              GraphicItem(defaultVisible, defaultOrigin, 0.0),
              FilledShape(
                Color.RED,
                "FillPattern.HorizontalCylinder",
                Color.BLACK,
                4.0,
                "LinePattern.Solid"
              ),
              "BorderPattern.None",
              ( (51,471),(400,299) )),
              RectangleElement(
                GraphicItem(defaultVisible, defaultOrigin, 0.0),
                FilledShape(
                  Color.RED,
                  "FillPattern.HorizontalCylinder",
                  Color.BLACK,
                  4.0,
                  "LinePattern.DashDot"
                ),
                "BorderPattern.None",
                ( (51,471),(400,299) )),
                RectangleElement(
                  GraphicItem(defaultVisible, defaultOrigin, 0.0),
                  FilledShape(
                    Color.RED,
                    "FillPattern.HorizontalCylinder",
                    Color.BLACK,
                    4.0,
                    "LinePattern.Dash"
                  ),
                  "BorderPattern.None",
                  ( (51,471),(400,299) ))

          ),NoPosition, NoPosition
          )
      )
      iconEqual(expErg2, erg2)

    val rect2 =
      s"""
         |Rectangle(
         |        lineColor = {50,45,20},
         |        fillColor = {255,30,100},
         |        lineThickness = 3.0,
         |        pattern = LinePattern.DashDot,
         |        fillPattern = FillPattern.VerticalCylinder,
         |        extent = {{100,300}, {400,100}}
         |      )
       """.stripMargin
    val erg3 = withParseSuccess(graphicModel("rect2", rect2))
    val exp3 =
      Model("rect2",
          Icon(
            None,
            List(RectangleElement(
              GraphicItem(defaultVisible, defaultOrigin, 0.0),
              FilledShape(
                new Color(255.0/255.0, 30.0/255.0, 100.0/255.0, 1.0),
                "FillPattern.VerticalCylinder",
                new Color(50.0/255.0, 45.0/255.0, 20.0/255.0, 1.0),
                3.0,
                "LinePattern.DashDot"
              ),
              "BorderPattern.None",
              ( (100,300),(400,100) )
            )),NoPosition, NoPosition
          )
      )

      iconEqual(exp3, erg3)

    val rect3 =
      s"""
         |model abc
         | annotation(
         |   Icon(
         |    coordinateSystem (
         |      extent = {{0,0},{756,504}}
         |    ),
         |   graphics = {
         |     Rectangle(
         |        lineColor = {0,0,0},
         |        fillColor = {255,0,0},
         |        lineThickness = 4.0,
         |        pattern = LinePattern.Solid,
         |        fillPattern = FillPattern.HorizontalCylinder,
         |        extent = {{51,471}, {400,299}}
         |      )
         |    })
         | );
         |end abc;
       """.stripMargin

    val erg33 = withParseSuccess(rect3)
    val exp33 =
      Model("abc",
          Icon(
            Some(CoordinateSystem(
              ( (0,0),(756,504) )
            )),
            List(expectedRect),NoPosition, NoPosition
          )
      )
    iconEqual(exp33,erg33)
  }

  it should "parse without ordering" in {
    val rect =
      """
        |model abc
        | annotation(
        |   Icon(
        |    coordinateSystem (
        |      extent = {{0,0},{756,504}}
        |    ),
        |   graphics = {
        |Rectangle(
        |origin = {5,20},
        |extent = {{100,300}, {400,100}},
        |        lineColor = {50,45,20},
        |
        |        pattern = LinePattern.DashDot,
        |        lineThickness = 3.0,
        |
        |        fillColor = {255,30,100},
        |        fillPattern = FillPattern.VerticalCylinder
        |      )
        |    })
        |    );
        |end abc;
      """.stripMargin

    val erg = withParseSuccess(rect)
    val expErg =
      Model("abc",
        Icon(
          Some(CoordinateSystem(((0.0,0.0),(756.0,504.0)))),
          List(
            RectangleElement(
              GraphicItem(defaultVisible, (5.0,20.0), 0.0),
              FilledShape(
                new Color(255.0/255.0, 30.0/255.0, 100.0/255.0, 1.0),
                "FillPattern.VerticalCylinder",
                new Color(50.0/255.0, 45.0/255.0, 20.0/255.0, 1.0),
                3.0,
                "LinePattern.DashDot"
              ),
              "BorderPattern.None",
              ( (100,300),(400,100) )
            ))
        ,NoPosition,NoPosition))
    iconEqual(expErg, erg)

    val rect2 =
      """
        |model abc
        | annotation(
        |   Icon(
        |    coordinateSystem (
        |      extent = {{0,0},{756,504}}
        |    ),
        |   graphics = {
        |Rectangle(
        |extent = {{100,300}, {400,100}},
        |        lineColor = {50,45,20,
        |
        |        pattern = LinePattern.DashDot,
        |        lineThickness = 3.0,
        |
        |        fillColor = {255,30,100},
        |        fillPattern = FillPattern.VerticalCylinder
        |      )
        |    })
        |    );
        |end abc;
      """.stripMargin

    withException(rect2)
  }

  it should "parse ellipses" in {
    val str =
      """
      |      Ellipse(
      |        lineColor = {0,0,0},
      |        fillColor = {255,0,0},
      |        lineThickness = 4.0,
      |        pattern = LinePattern.DashDot,
      |        fillPattern = FillPattern.VerticalCylinder,
      |        extent = {{100,239},{342,25}}
      |      )
      """.stripMargin

      val expEllipse =
        Model("ellipse",
        Icon(
          None,
          List(
            Ellipse(
              GraphicItem(defaultVisible, defaultOrigin),
              FilledShape(
                Color.RED,
                "FillPattern.VerticalCylinder",
                Color.BLACK,
                4.0,
                "LinePattern.DashDot"
              ),
              ( (100,239),(342,25) )
            )),NoPosition,NoPosition
        )
        )

      iconEqual(expEllipse, withParseSuccess(graphicModel("ellipse", str)))

      val errorStr =
        """
        |      Ellipse(
        |        lineColor = {0,0,0},
        |        fillColor = {255,0,0},
        |        lineThickness = 4.0,
        |        pattern = LinePattern.DashDot,
        |        fillPattern = FillPattern.VerticalCylinder
        |      )
        """.stripMargin

      withException(errorStr)


      val str2 =
        """
        |      Ellipse(
        |        fillColor = {255,0,0},
        |        extent = {{100,239},{342,25}},
        |        lineThickness = 4.0,
        |        lineColor = {0,0,0},
        |        fillPattern = FillPattern.VerticalCylinder,
        |        pattern = LinePattern.DashDot
        |      )
        """.stripMargin

      iconEqual(expEllipse, withParseSuccess(graphicModel("ellipse", str2)))

      val minimalEllipse =
        """
        |      Ellipse(
        |        extent = {{100,239},{342,25}}
        |      )
        """.stripMargin

      val minimalExp =
      Model("ellipse2",
      Icon(
        None,
        List(Ellipse(
            GraphicItem(),
            FilledShape(),
            ( (100,239),(342,25) )
          )),NoPosition,NoPosition
        )
      )

      iconEqual(minimalExp, withParseSuccess(graphicModel("ellipse2", minimalEllipse)))
  }

  it should "parse lines" in {
    val lineStr =
      """
      |      Line(
      |        color = {0,255,0},
      |        thickness = 2.0,
      |        pattern = LinePattern.None,
      |        points = {{100,239},{342,25}, {50,50}},
      |        smooth = Smooth.Bezier,
      |        arrow = { Arrow.None, Arrow.None}
      |      )
      """.stripMargin

      val lineExp =
        Model("Lines",
        Icon(
          None,
          List(PathElement(
            GraphicItem(),
            List( (100.0,239.0),(342.0,25.0),(50.0,50.0) ),
            new Color(0,1,0, 1), //= rgb(0,255,0) = lightest green
            2.0,
            "LinePattern.None",
            "Smooth.Bezier"
          )
        ),NoPosition,NoPosition
      )
    )
    iconEqual(lineExp, withParseSuccess(graphicModel("Lines", lineStr)))

    val lineStr2 =
      """
      |      Line(
      |        color = {0,255,0},
      |        thickness = 2.0,
      |        pattern = LinePattern.None,
      |        points = {},
      |        smooth = Smooth.Bezier
      |      )
      """.stripMargin

      withException(graphicModel("Lines", lineStr2))

      val lineStr3 =
        """
        |      Line(
        |        color = {0,255,0},
        |        thickness = 2.0,
        |        pattern = LinePattern.None,
        |        smooth = Smooth.Bezier
        |      )
        """.stripMargin

        withException(graphicModel("Lines", lineStr3))

  }

  it should "parse polygons" in {
    val polyStr =
      """
        |model poly
        | annotation(
        |   Icon( graphics = {
        |     Polygon(
        |        lineColor = {0,0,0},
        |        fillColor = {0,0,255},
        |        lineThickness = 2.0,
        |        pattern = LinePattern.Solid,
        |        fillPattern = FillPattern.VerticalCylinder,
        |        points = { {5,10},{20,20},{50,50} },
        |        smooth = Smooth.Bezier
        |      )
        |   })
      |   );
        |end poly;
      """.stripMargin

      val polyExp =
      Model("poly",
      Icon(
        None,
        List(Polygon(
          GraphicItem(),
          FilledShape(
            new Color(0.0,0.0,1.0,1.0),
            "FillPattern.VerticalCylinder",
            Color.BLACK,
            2.0,
            "LinePattern.Solid"
            ),
            List( (5.0,10.0),(20.0,20.0),(50.0,50.0) ),
            "Smooth.Bezier"
        )
      ),NoPosition,NoPosition
    ))
    iconEqual(polyExp, withParseSuccess(polyStr))

    val minimalPoly =
    """
      |model poly
      | annotation(
      |   Icon( graphics = {
      |     Polygon(
      |        points = { {5,10},{20,20},{50,50} }
      |      )
      |   })
    |   );
      |end poly;
    """.stripMargin

    val minPolyExp =
      Model("poly",
      Icon(
        None,
        List(Polygon(
          GraphicItem(),
          FilledShape(),
          List( (5.0,10.0),(20.0,20.0),(50.0,50.0) ),
          "Smooth.None"
        )
      ),NoPosition,NoPosition
      ))

      iconEqual(minPolyExp, withParseSuccess(minimalPoly))

  }

  it should "parse bitmaps" in {
    val imgStr =
       """
       |Bitmap(
       | visible = true,
       | origin = {0,0},
       | extent = { {0,0},{50,100} },
       | fileName = "modelica://test/quokka.png"
       |)
       """.stripMargin

    val expImg =
      Model("img",
      Icon(
        None,
        List(ImageURI(
          GraphicItem(),
          extent = ( (0,0),(50,100) ),
          "modelica://test/quokka.png"
          )
          )
      ,NoPosition,NoPosition))
    iconEqual(expImg, withParseSuccess(graphicModel("img", imgStr)))

    val imgStr2 =
      """
      |Bitmap(
      | extent = { {0,0},{50,100} },
      | fileName = "modelica://test/quokka.png"
      |)
      """.stripMargin

    iconEqual(expImg, withParseSuccess(graphicModel("img", imgStr2)))

    val imgStr3 =
      """
      |Bitmap(
      | visible = true,
      | origin = {0,0},
      | extent = { {0,0},{50,100} },
      | imageSource = "5546659985164adkcölkhas"
      |)
      """.stripMargin

    val img3Exp =
    Model("img",
    Icon(
      None,
      List(ImageBase64(
        GraphicItem(),
        extent = ( (0,0),(50,100) ),
        "5546659985164adkcölkhas"
        )
        )
    ,NoPosition,NoPosition))

    iconEqual(img3Exp, withParseSuccess(graphicModel("img", imgStr3)))

    val imgStr4 =
      """
      |Bitmap(
      | extent = { {0,0},{50,100} },
      | imageSource = "5546659985164adkcölkhas"
      |)
      """.stripMargin

    iconEqual(img3Exp, withParseSuccess(graphicModel("img", imgStr4)))
  }

  it should "fail on falsy models" in {
    val model =
      """
        |model test
        | annotation(
        |   Icon (
        |     graphics = {}
        |   )
        | );
        |end test3;
      """.stripMargin

    withException(model)

    val model2 =
      """
        |model test
        | annotation(
        |   Icon (
        |     graphics = {}
        |
        | );
        |end test;
      """.stripMargin
    withException(model2)

    val model3 =
      s"""
         |model abc
         | annotation(
         |   Icon(
         |    coordinateSystem (
         |      extent = {{0,0},{756,504}
         |    )
         |   graphics = {
         |     Rectangle(
         |        lineColor = {0,0,0},
         |        fillColor = {255,0,0},
         |        lineThickness = 4.0,
         |        pattern = LinePattern.Solid,
         |        fillPattern = FillPattern.HorizontalCylinder,
         |        extent = {{51,471}, {400,299}}
         |      )
       |      })
         |end abc;
       """.stripMargin

    withException(model3)
  }

  it should "parse rotated rectangles" in {
    val rect =
      """
        |model abc
        | annotation(
        |   Icon( graphics = {
        |     Rectangle(
        |        pattern = LinePattern.Solid,
        |        fillPattern = FillPattern.HorizontalCylinder,
        |        lineColor = {0,0,0},
        |        fillColor = {255,0,0},
        |        extent = {{51,471}, {400,299}},
        |        rotation = 45.0
        |      )
        |   })
        |   );
        |end abc;
      """.stripMargin
    val erg = withParseSuccess(rect)

    val exp =
      Model("abc",
          Icon(None,
            List(
              RectangleElement(
                GraphicItem(rotation=45.0),
                FilledShape(Color.RED,
                  "FillPattern.HorizontalCylinder",
                  Color.BLACK,
                  1.0,
                  "LinePattern.Solid"
                  ),
                  "BorderPattern.None",
                  ( (51,471),(400,299))
                )
              ),NoPosition,NoPosition
            )
      )

      iconEqual(exp, withParseSuccess(rect))
  }

  it should "parse text" in {
    val txtStr =
      s"""Text(
      |origin = {437,426},
      |extent = {{-222,23},{222,-23}},
      |textString = "This is a beatuiful test",
      |fontSize = 12.0,
      |fontName = "System",
      |textStyle = {TextStyle.Italic},
      |textColor = {0,0,0},
      |horizontalAlignment = TextAlignment.Left
      |)""".stripMargin

    val ast1 = Model("txt1",
    Icon(
      None,
      List(Text(
        GraphicItem(origin=(437,426)),
        ((-222,23), (222,-23)),
        "This is a beatuiful test",
        12,"System", Seq("TextStyle.Italic"),
        Color.BLACK, "TextAlignment.Left"))
      ,NoPosition,NoPosition))

    iconEqual(ast1, withParseSuccess(graphicModel("txt1", txtStr)))

    val txtStr2 =
      s"""Text(
      |origin = {437,426},
      |extent = {{-222,23},{222,-23}},
      |textString = "This is a beatuiful test",
      |fontSize = 12.0,
      |fontName = "Courier",
      |textStyle = {TextStyle.Italic, TextStyle.Bold},
      |textColor = {255,0,0},
      |horizontalAlignment = TextAlignment.Left
      |)""".stripMargin

      val ast2 = Model("txt2",
      Icon(
        None,
        List(Text(
          GraphicItem(origin=(437,426)),
          ((-222,23), (222,-23)),
          "This is a beatuiful test",
          12,"Courier", Seq("TextStyle.Italic", "TextStyle.Bold"),
          Color.RED, "TextAlignment.Left"))
        ,NoPosition,NoPosition))

    iconEqual(ast2, withParseSuccess(graphicModel("txt2", txtStr2)))
  }
}
