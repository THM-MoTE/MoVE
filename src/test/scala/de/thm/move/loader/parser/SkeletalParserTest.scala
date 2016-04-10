package de.thm.move.loader.parser

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import javafx.scene.paint.Color

import org.junit.Assert._
import org.junit.Test

import de.thm.move.loader.parser.PropertyParser._
import de.thm.move.loader.parser.ast._

class SkeletalParserTest {

  @Test
  def baseTest:Unit = {
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
        List(
          Icon(None, Nil)
        ))
    assertEquals(modelTestExpec, modelTestErg)

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
        GraphicItem(defaultVisible, defaultOrigin, Rotation()),
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
        List(
          Icon(
            None,
            List(
              expectedRect
            )
          )
        )
      )

    assertEquals(rectExp, erg)

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
        List(
          Icon(
            None,
            rectangles
          )
        )
      )

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
        List(
          Icon(
            None,
            List(RectangleElement(
              GraphicItem(defaultVisible, defaultOrigin, Rotation()),
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
          )
        )
      )

      assertEquals(exp3, erg3)

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
        List(
          Icon(
            Some(CoordinateSystem(
              ( (0,0),(756,504) )
            )),
            List(expectedRect)
          )
        )
      )
  }

  @Test
  def orderDontMatter: Unit = {
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
        List(Icon(
          Some(CoordinateSystem(((0.0,0.0),(756.0,504.0)))),
          List(
            RectangleElement(
              GraphicItem(defaultVisible, (5.0,20.0), Rotation()),
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
        ))
        )
    assertEquals(expErg, erg)

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

  @Test
  def ellipseTest:Unit = {
    val str =
      """
      |graphics = {
      |      Ellipse(
      |        lineColor = {0,0,0},
      |        fillColor = {255,0,0},
      |        lineThickness = 4.0,
      |        pattern = LinePattern.DashDot,
      |        fillPattern = FillPattern.VerticalCylinder,
      |        extent = {{100,239},{342,25}},
      |        endAngle = 360
      |      )
      |}
      """.stripMargin
  }

  @Test
  def modelFailure: Unit = {
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
         |      extent = {{0,0},{756,504}}
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
}
