package de.thm.move.loader.parser

import javafx.scene.paint.Color

import de.thm.move.loader.parser.ModelicaParserLike.ParsingError
import de.thm.move.models.CommonTypes._

import scala.util.parsing.combinator.RegexParsers

trait PropertyParser {
  self: RegexParsers =>

  // regex from: http://stackoverflow.com/a/5954831
  override val whiteSpace = """(\s|//.*|(?m)/\*(\*(?!/)|[^*])*\*/)+""".r
  protected val identRegex = "[a-zA-Z_][a-zA-Z0-9_\\.]*".r
  protected val numberRegex = "-?[0-9]+".r

  def properties(parsers:Parser[(String, String)]*):Parser[Map[String,String]] = {
    val oredParser = parsers.tail.foldLeft(parsers.head)(_|_)
    val propertiesListParser = rep(oredParser)
    propertiesListParser.map(_.toMap)
  }

  def propertyKeys(keys:String*):Parser[Map[String,String]] = properties(keys.map(property):_*)

  def property(key:String):Parser[(String,String)] =
    (key <~ "=") ~ value ^^ { case k ~ v => (k,v) }

  def getPropertyValue[A](map:Map[String,String], key:String)(parser:Parser[A], default: => A): A =
    map.get(key).map(parse(parser,_)).map {
      case Success(v,_) => v
      case NoSuccess(msg,_) => throw new ParsingError(msg)
    }.getOrElse(default)

  def getPropertyValue[A](map:Map[String,String], key:String)(parser:Parser[A]): A =
    map.get(key).map(parse(parser,_)).map {
      case Success(v,_) => v
      case NoSuccess(msg,_) => throw new ParsingError(msg)
    }.getOrElse(throw new ParsingError("property $key has to be defined!"))
  
  val value:Parser[String] = ".+".r
/*
  val lineColor = "lineColor" ~> "=" ~> color
  val linePattern = "pattern" ~> "=" ~> identRegex
  val fillColor = "fillColor" ~> "=" ~> color
  val fillPattern = "fillPattern" ~> "=" ~> identRegex
  val extent = "extent" ~> "=" ~> "{" ~> rep1sep(point, ",") <~ "}"
  val lineThickness = "lineThickness" ~> "=" ~> decimalNo
*/
  def extension:Parser[(Point,Point)] =
    "extent" ~> "=" ~> ("{"~> point <~ ",") ~ point <~ "}" ^^ { case p1 ~ p2 => (p1,p2) }


  def point:Parser[Point] =
    ("{" ~> numberParser <~ ",") ~ numberParser <~ "}" ^^ {
      case x ~ y => (x,y)
    }

  def points:Parser[List[Point]] = "{" ~> rep1sep(point, ",") <~ "}"

  def color:Parser[Color] =
    ("{" ~> numberParser <~ ",") ~ (numberParser <~ ",") ~ numberParser <~ "}" ^^ {
      case r ~ g ~ b => new Color(r/255,g/255,b/255, 1.0)
    }

  def numberParser:Parser[Double] = numberRegex ^^ { _.toDouble }
  val decimalNo:Parser[Double]
}

object PropertyParser {
  //property keys
  val lineCol = "lineColor"
  val linePatt = "pattern"
  val fillCol = "fillColor"
  val fillPatt = "fillPattern"
  val extent = "extent"
  val lineThick = "lineThickness"

  //default values
  val defaultCol = Color.BLACK
  val defaultLinePatt = "LinePattern.Solid"
  val defaultFillPatt = "FillPattern.Solid"
  val defaultLineThick = 1.0
  val defaultRectangleExtent = List( (0.0,0.0), (0.0,0.0) )
}
