package de.thm.move.loader.parser

import javafx.scene.paint.Color

import de.thm.move.loader.parser.ModelicaParserLike.ParsingError

import scala.util.parsing.combinator.{JavaTokenParsers, ImplicitConversions}
import scala.language.postfixOps
import scala.util._
import java.io.BufferedInputStream
import java.io.InputStreamReader
import java.io.InputStream

import de.thm.move.models.CommonTypes._
import de.thm.move.util.PointUtils._

class ModelicaParser extends JavaTokenParsers with ImplicitConversions with ModelicaParserLike {
  // regex from: http://stackoverflow.com/a/5954831
  override val whiteSpace = """(\s|//.*|(?m)/\*(\*(?!/)|[^*])*\*/)+""".r
  private val identRegex = "[a-zA-Z_][a-zA-Z0-9_\\.]*".r
  private val numberRegex = "-?[0-9]+".r

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

  def extension:Parser[(Point,Point)] =
    "extent" ~> "=" ~> ("{"~> point <~ ",") ~ point <~ "}" ^^ { case p1 ~ p2 => (p1,p2) }

  def point:Parser[Point] =
    ("{" ~> numberParser <~ ",") ~ numberParser <~ "}" ^^ {
      case x ~ y => (x,y)
    }


  def graphic:Parser[List[ShapeElement]] =
   "graphics" ~>  "=" ~> "{" ~> repsep(graphics, ",") <~ "}"

  def graphics:Parser[ShapeElement] = (
    "Rectangle" ~> "(" ~> rectangleFields <~ ")"
    )

  def rectangleFields:Parser[RectangleElement] =
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
    }

  val lineColor = "lineColor" ~> "=" ~> color
  val linePattern = "pattern" ~> "=" ~> identRegex
  val fillColor = "fillColor" ~> "=" ~> color
  val fillPattern = "fillPattern" ~> "=" ~> identRegex
  val extent = "extent" ~> "=" ~> "{" ~> rep1sep(point, ",") <~ "}"
  val lineThickness = "lineThickness" ~> "=" ~> decimalNo

  def color:Parser[Color] =
    ("{" ~> numberParser <~ ",") ~ (numberParser <~ ",") ~ numberParser <~ "}" ^^ {
     case r ~ g ~ b => new Color(r/255,g/255,b/255, 1.0)
   }

  def numberParser:Parser[Double] = numberRegex ^^ { _.toDouble }
  val decimalNo = decimalNumber ^^ { _.toDouble }

  def parse(stream:InputStream): Try[Model] = Try {
    parseAll(start, new InputStreamReader(new BufferedInputStream(stream))) match {
      case Success(result, _)    => result
      case NoSuccess(msg, input) => throw new ParsingError(msg)
    }
  }
}
