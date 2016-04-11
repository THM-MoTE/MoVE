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
  protected val javaLikeStrRegex = "\"(.*)\"".r

  @annotation.tailrec
  private def containsDuplicates[A](xs:List[A], seen:Set[A] = Set[A]()): Boolean = xs match {
    case hd::tl if seen.contains(hd) => true
    case hd::tl => containsDuplicates(tl, seen + hd)
    case Nil => false
  }

  def properties(parsers:Parser[(String, String)]*):Parser[Map[String,String]] = {
    val oredParser = parsers.tail.foldLeft(parsers.head)(_|_)

    val propertiesListParser = repsep(oredParser, ",")
    propertiesListParser.map { tuples =>
      if(containsDuplicates(tuples)) throw new ParsingError("Duplicate definitions of properties!")
      else tuples.toMap
    }
  }

  def propertyKeys(keys:String*):Parser[Map[String,String]] = properties(keys.map(property):_*)

  def property(key:String):Parser[(String,String)] =
    (key <~ "=") ~ value ^^ { case k ~ v => (k,v) }

  def getPropertyValue[A](map:Map[String,String], key:String, default: => A)(parser:Parser[A]): A =
    map.get(key).map(parse(parser,_)).map {
      case Success(v,_) => v
      case NoSuccess(msg,_) =>
        throw new ParsingError(msg)
    }.getOrElse(default)

  def getPropertyValue[A](map:Map[String,String], key:String)(parser:Parser[A]): A =
    map.get(key).map(parse(parser,_)).map {
      case Success(v,_) => v
      case NoSuccess(msg,_) => throw new ParsingError(msg)
    }.getOrElse(throw new ParsingError(s"""property "$key" has to be defined!"""))

  val value:Parser[String] = (
    identRegex
    | javaLikeStrRegex
    | numberRegex ~ "." ~ numberRegex ^^ { case n1~comma~n2 => n1+comma+n2 }
    | numberRegex
    | "{" ~ repsep(numberRegex|identRegex, ",") ~ "}" ^^ { case lp~inner~rp => lp+inner.mkString(",")+rp }
    | "{" ~ repsep(value, ",") ~ "}" ^^ { case lp~inner~rp => lp+inner.mkString(",")+rp }
  )
/*
  val lineColor = "lineColor" ~> "=" ~> color
  val linePattern = "pattern" ~> "=" ~> identRegex
  val fillColor = "fillColor" ~> "=" ~> color
  val fillPattern = "fillPattern" ~> "=" ~> identRegex
  val extent = "extent" ~> "=" ~> "{" ~> rep1sep(point, ",") <~ "}"
  val lineThickness = "lineThickness" ~> "=" ~> decimalNo
*/
  def extension:Parser[(Point,Point)] =
    ("{"~> point <~ ",") ~ point <~ "}" ^^ { case p1 ~ p2 => (p1,p2) }

  def point:Parser[Point] =
    ("{" ~> numberParser <~ ",") ~ numberParser <~ "}" ^^ {
      case x ~ y => (x,y)
    }

  def arrow:Parser[(String, String)] =
    ("{"~> ident <~ ",") ~ ident <~ "}" ^^ { case a1 ~ a2 => (a1,a2) }

  def points:Parser[List[Point]] = "{" ~> rep1sep(point, ",") <~ "}"

  def color:Parser[Color] =
    ("{" ~> numberParser <~ ",") ~ (numberParser <~ ",") ~ numberParser <~ "}" ^^ {
      case r ~ g ~ b => new Color(r/255,g/255,b/255, 1.0)
    }

  def base64OrRsc:Parser[String] = "fileName" | "imageSource"

  def numberParser:Parser[Double] = numberRegex ^^ { _.toDouble }
  val decimalNo:Parser[Double]
  val ident:Parser[String]
  val bool:Parser[Boolean] = ("true" | "false") ^^ {
    case "true" => true
    case "false" => false
  }
}

object PropertyParser {
  //property keys
  val visible = "visible"
  val origin = "origin"
  val lineCol = "lineColor"
  val linePatt = "pattern"
  val fillCol = "fillColor"
  val fillPatt = "fillPattern"
  val extent = "extent"
  val lineThick = "lineThickness"
  val radius = "radius"
  val colorKey = "color"
  val arrowKey = "arrow"
  val arrowSize = "arrowSize"
  val smooth = "smooth"
  val pointsKey = "points"
  val base64 = "imageSource"
  val imgUri = "fileName"
  val thick = "thickness"
  val endAngle = "endAngle"
  val rotation = "rotation"

  //default values
  val defaultVisible = true
  val defaultOrigin = (0.0,0.0)
  val defaultCol = Color.BLACK
  val defaultLinePatt = "LinePattern.Solid"
  val defaultFillPatt = "FillPattern.None"
  val defaultLineThick = 1.0
  val defaultRectangleExtent = List( (0.0,0.0), (0.0,0.0) )
  val defaultRadius = 0.0
  val defaultArrow = ("Arrow.None", "Arrow.None")
  val defaultArrowSize = 3.0
  val defaultSmooth = "Smooth.None"
  val defaultEndAngle = 360.0
  val defaultRotation = 0.0
}
