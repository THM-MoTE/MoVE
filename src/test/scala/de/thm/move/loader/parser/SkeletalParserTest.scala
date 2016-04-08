package de.thm.move.loader.parser

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

import org.junit.Assert._
import org.junit.Test

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

    withParseSuccess(modelTest)

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
    println(erg)

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

    println(erg2)

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
    println(erg3)

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
    println(erg33)
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
