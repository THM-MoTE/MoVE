package de.thm.move.loader.parser

import javafx.scene.paint.Color

import de.thm.move.loader.parser.ModelicaParserLike.ParsingError
import de.thm.move.loader.parser.PropertyParser._

import scala.util.parsing.combinator.{JavaTokenParsers, ImplicitConversions}
import scala.language.postfixOps
import scala.util._
import java.io.BufferedInputStream
import java.io.InputStreamReader
import java.io.InputStream

import de.thm.move.models.CommonTypes._
import de.thm.move.util.PointUtils._


class ModelicaParser extends JavaTokenParsers with ImplicitConversions with ModelicaParserLike with PropertyParser {
  val decimalNo = decimalNumber ^^ { _.toDouble }

  override val ident:Parser[String] = identRegex

  val start = model
  def model:Parser[Model] =
    ("model" ~> identRegex) ~ annotation ~ ("end" ~> identRegex <~ ";") ^^ {
      case startIdent ~ annot ~ endIdent =>
        if(startIdent == endIdent) Model(startIdent, annot)
        else throw new ParsingError(s"Modelname at end of file doesn't match starting modelname! ($startIdent != $endIdent)")
    }

  def annotation:Parser[List[Annotation]] = "annotation" ~> "(" ~> annotations <~ ")" <~ ";"
  def annotations:Parser[List[Annotation]] = (
    (icon +)
    )

  def icon:Parser[Icon] =
    ("Icon" ~> "(") ~>
      ((coordinateSys <~ ",") ?) ~
        graphic <~
        ")" ^^  { case coord ~ graphics => Icon(coord, graphics) }

  def coordinateSys: Parser[CoordinateSystem] =
    "coordinateSystem" ~>"(" ~> extension <~ ")" ^^ CoordinateSystem

  def graphic:Parser[List[ShapeElement]] =
   "graphics" ~>  "=" ~> "{" ~> repsep(graphics, ",") <~ "}"

  def graphics:Parser[ShapeElement] = (
    "Rectangle" ~> "(" ~> rectangleFields <~ ")"
    )

  /*def rectangleFields:Parser[RectangleElement] =
    (lineColor  <~ ",") ~
    (fillColor <~ ",") ~
    (lineThickness <~ ",") ~
    (linePattern <~ ",") ~
    (fillPattern <~ ",") ~
    extent ^^ {
      case lCol ~ fCol ~ lThik ~ lp ~ fp ~ ext =>
        val start = ext.head
        val endP = ext.tail.head
        val w = endP.x
        val h = endP.y
        RectangleElement(start,w,h,fCol,fp, lCol,lThik, lp)
    }*/

  def rectangleFields:Parser[RectangleElement] =
    propertyKeys(lineCol,linePatt,fillCol,
      fillPatt,extent,lineThick) ^^ { map =>
      val lCol = getPropertyValue(map, lineCol)(color, defaultCol)
      val fCol = getPropertyValue(map, fillCol)(color, defaultCol)
      val lPatt = getPropertyValue(map, linePatt)(ident, defaultLinePatt)
      val fPatt = getPropertyValue(map, fillPatt)(ident, defaultFillPatt)
      val lThick = getPropertyValue(map, lineThick)(numberParser, defaultLineThick)
      val ext = getPropertyValue(map, extent)(points, defaultRectangleExtent)
      val start = ext.head
      val end = ext.tail.head
      val w = end.x
      val h = end.y
      RectangleElement(start,w,h,fCol,fPatt,lCol, lThick, lPatt)
    }

  def parse(stream:InputStream): Try[Model] = Try {
    parseAll(start, new InputStreamReader(new BufferedInputStream(stream))) match {
      case Success(result, _)    => result
      case NoSuccess(msg, input) => throw new ParsingError(msg)
    }
  }
}
