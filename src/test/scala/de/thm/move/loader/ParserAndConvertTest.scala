package de.thm.move.loader

import javafx.scene.paint.Color

import de.thm.move.loader.parser.PropertyParser._
import de.thm.move.views.shapes._
import de.thm.move.util.PointUtils._
import parser._
import org.junit.Test
import org.junit.Assert._

class ParserAndConvertTest {

  @Test
  def parseRectangle():Unit = {
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
    val convertedRectangle = conv.getShapes(parsed).head.asInstanceOf[ResizableRectangle]

    assertEquals((0,(500)), convertedRectangle.getXY)
    assertEquals((50), convertedRectangle.getWidth, 0.01)
    assertEquals((50), convertedRectangle.getHeight, 0.01)


    val multiplier = 4
    val conv2 = new ShapeConverter(multiplier, ShapeConverter.gettCoordinateSystemSizes(parsed), null)
    val convertedRectangle2 = conv2.getShapes(parsed).head.asInstanceOf[ResizableRectangle]

    assertEquals((0,500*multiplier), convertedRectangle2.getXY)
    assertEquals(50*multiplier, convertedRectangle2.getWidth, 0.01)
    assertEquals(50*multiplier, convertedRectangle2.getHeight, 0.01)
    assertEquals(Color.RED, convertedRectangle2.getFillColor)
    assertEquals(2.0, convertedRectangle2.getStrokeWidth, 0.01)
    assertEquals(Color.BLACK, convertedRectangle2.getStrokeColor)

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
    val convertedRectangle3 = conv.getShapes(parsed2).head.asInstanceOf[ResizableRectangle]

    assertEquals((20,500-60), convertedRectangle3.getXY)
    assertEquals(100-20, convertedRectangle3.getWidth, 0.01)
    assertEquals(150-60, convertedRectangle3.getHeight, 0.01)
  }
}
