package de.thm.move.loader

import javafx.scene.paint.Color

import de.thm.move.models.LinePattern
import de.thm.move.views.shapes._
import org.junit.Assert._
import org.junit.Test

import de.thm.move.loader.parser.PropertyParser._
import de.thm.move.loader.parser.ast._
import de.thm.move.util.PointUtils._
import de.thm.move.util.GeometryUtils
import de.thm.move.util.GeometryUtils._
import de.thm.move.models.CommonTypes._

import scala.util.parsing.input.Position
import scala.util.parsing.input.NoPosition

class ConverterTest {

  @Test
  def convertCoordinationSystem: Unit = {

    val extent = ( ((-100.0),(-50.0)),((100.0),(50.0)) )
    val ast = Model("ölkj",
    Icon(Some(CoordinateSystem(extent)), List(), NoPosition, NoPosition)
    )
    val conv = new ShapeConverter(4, ShapeConverter.gettCoordinateSystemSizes(ast), null)

    val exp = (200,100)
    assertEquals(exp, ShapeConverter.gettCoordinateSystemSizes(ast) )

    val ast2 = Model("ög",
      Icon(Some(CoordinateSystem(extent)), List(), NoPosition, NoPosition)
    )

    val x = ShapeConverter.gettCoordinateSystemSizes(ast)
    assertEquals(exp, x)

    val extent2 = ( ((-100.0),(-50.0)),((0.0),(-20.0)) )
    val ast3 = Model("ölkj",
    Icon(Some(CoordinateSystem(extent2)), List(), NoPosition, NoPosition)
    )
    val exp2 = (100.0, 30)
    assertEquals(exp2, ShapeConverter.gettCoordinateSystemSizes(ast3) )
  }

  @Test
  def convertLine:Unit = {
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

    val convertedLine = conv.getShapes(ast).head.asInstanceOf[ResizableLine]
    val startAnchor = convertedLine.getAnchors.head
    val endAnchor = convertedLine.getAnchors.tail.head

    assertEquals( 10.0, startAnchor.getCenterX, 0.01 )
    assertEquals( 200.0-10.0, startAnchor.getCenterY, 0.01 )
    assertEquals( 50.0, endAnchor.getCenterX, 0.01 )
    assertEquals( 200.0-30.0, endAnchor.getCenterY, 0.01 )
    assertEquals( Color.BLACK, convertedLine.getStrokeColor )
    assertEquals( 1.0, convertedLine.getStrokeWidth, 0.01 )
    assertEquals( LinePattern.Dash, convertedLine.linePattern.get )


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
    val convPath = conv.getShapes(ast2).head.asInstanceOf[ResizablePath]
    convPath.getAnchors.zip(expectedPoints).foreach {
      case (p1,p2) => assertEquals((p1.getCenterX,p1.getCenterY),p2)
    }

    val converter = new ShapeConverter(5, ShapeConverter.gettCoordinateSystemSizes(ast2), null)
    val convPath2 = converter.getShapes(ast2).head.asInstanceOf[ResizablePath]
    convPath2.getAnchors.zip(expectedPoints.map(_.map(_*5))).foreach {
      case (p1,p2) => assertEquals(p2, (p1.getCenterX,p1.getCenterY))
    }
  }

  @Test
  def convertRectangle:Unit = {
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
    val convRec = conv.getShapes(ast).head.asInstanceOf[ResizableRectangle]
    assertEquals((205,defaultCoordinateSystemSize.y-179), convRec.getXY)
    assertEquals(348-205, convRec.getWidth, 0.01)
    assertEquals(179-36, convRec.getHeight, 0.01)

    val multiplier = 5
    val conv2 = new ShapeConverter(multiplier, ShapeConverter.gettCoordinateSystemSizes(ast), null)
    val convRec2 = conv2.getShapes(ast).head.asInstanceOf[ResizableRectangle]
    assertEquals((205*multiplier,(defaultCoordinateSystemSize.y-179)*multiplier), convRec2.getXY)
    assertEquals((348-205)*multiplier, convRec2.getWidth, 0.01)
    assertEquals((179-36)*multiplier, convRec2.getHeight, 0.01)
  }

  @Test
  def convertCircle:Unit = {
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
    val convCircle = conv.getShapes(ast).head.asInstanceOf[ResizableCircle]
    val middleP = GeometryUtils.middleOfLine(205,
      defaultCoordinateSystemSize.y-179,
      348, defaultCoordinateSystemSize.y-36
      )
    assertEquals(348-205, convCircle.getWidth, 1.0)
    assertEquals(179-36, convCircle.getHeight, 1.0)

    val multiplier = 2
    val conv2 = new ShapeConverter(multiplier, ShapeConverter.gettCoordinateSystemSizes(ast), null)
    val conv2Circle = conv2.getShapes(ast).head.asInstanceOf[ResizableCircle]

    val middleP2 = GeometryUtils.middleOfLine(205*multiplier,
      (defaultCoordinateSystemSize.y-179)*multiplier,
      348*multiplier, (defaultCoordinateSystemSize.y-36)*multiplier
      )

    assertEquals(middleP2, conv2Circle.getXY)
    assertEquals((348-205)*multiplier, conv2Circle.getWidth, 1.0)
    assertEquals((179-36)*multiplier, conv2Circle.getHeight, 1.0)
  }

  @Test
  def convertPolygon:Unit = {
    val points = List( (205.0,179.0),(348.0,36.0),(420.0,50.0) )
    val ast = Model("ölk",
      Icon(None,
        List(
          Polygon(GraphicItem(),
            FilledShape(),
            points
          )
        ),NoPosition, NoPosition
      )
    )

    val expPoints = points.map {
      case (x,y) => (x, defaultCoordinateSystemSize.y-y)
    }

    val conv = new ShapeConverter(1, ShapeConverter.gettCoordinateSystemSizes(ast), null)
    val convPolygon = conv.getShapes(ast).head.asInstanceOf[ResizablePolygon]
    convPolygon.getAnchors.zip(expPoints).foreach {
      case (anchor,p2) =>
      val p1 = (anchor.getCenterX,anchor.getCenterY)
      assertEquals(p2,p1)
    }

    val multiplier = 4
    val conv2 = new ShapeConverter(multiplier, ShapeConverter.gettCoordinateSystemSizes(ast), null)
    val conv2Polygon = conv2.getShapes(ast).head.asInstanceOf[ResizablePolygon]
    conv2Polygon.getAnchors.zip(expPoints.map(_.map(_*multiplier))).foreach {
      case (anchor,p2) =>
        val p1 = (anchor.getCenterX,anchor.getCenterY)
        assertEquals(p2,p1)
    }

  }

  @Test
  def convertBitmap:Unit = {
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

  @Test
  def rectangleWithorigin: Unit = {
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
    val rect = conv.getShapes(ast).head.asInstanceOf[ResizableRectangle]

    val expXY:Point = (10-10,defaultCoordinateSystemSize.y-(10+50))
    val expW = 10+30
    val expH = 50+40

    assertEquals(expXY, rect.getXY)
    assertEquals(expW, rect.getWidth, 0.01)
    assertEquals(expH, rect.getHeight, 0.01)

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

      val rect2 = conv2.getShapes(ast2).head.asInstanceOf[ResizableRectangle]
      val expXY2:Point = (50-5,defaultCoordinateSystemSize.y-(30+70))
      val expW2 = 15
      val expH2 = 70+20

      assertEquals(expXY2.map(_*multiplier), rect2.getXY)
      assertEquals(expW2*multiplier, rect2.getWidth, 0.01)
      assertEquals(expH2*multiplier, rect2.getHeight, 0.01)
  }

  @Test
  def circleWithOrigin:Unit = {
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
    val circ = conv.getShapes(ast).head.asInstanceOf[ResizableCircle]

    val expCenterXY = (origin.x, defaultCoordinateSystemSize.y-origin.y)
    val expWRadius = asRadius(10+30)
    val expHRadius = asRadius(50+40)

    assertEquals(expWRadius, circ.getRadiusX, 0.01)
    assertEquals(expHRadius, circ.getRadiusY, 0.01)
    assertEquals(40, circ.getBoundsInLocal.getMinX, 1.0)
    assertEquals(defaultCoordinateSystemSize.y - 150, circ.getBoundsInLocal.getMinY, 1.0)
    assertEquals(80, circ.getBoundsInLocal.getMaxX, 1.0)
  }
}
