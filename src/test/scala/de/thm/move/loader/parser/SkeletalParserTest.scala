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
        |      )
        |   })
        |   );
        |end abc;
      """.stripMargin

    val erg2 = withParseSuccess(rectRect)

    println(erg2)
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

  }
}
