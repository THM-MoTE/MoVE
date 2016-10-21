/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.loader.parser

import javafx.scene.paint.Color

import de.thm.move.loader.parser.ModelicaParserLike.ParsingError
import de.thm.move.models.CommonTypes._
import de.thm.move.util.{Validation, ValidationSuccess, ValidationWarning}

import scala.util.parsing.combinator.RegexParsers

/** Defines parsers for all properties/fields that a modelica primitive can have. */
trait PropertyParser {
  self: RegexParsers =>

  type StringValidation[A] = Validation[A, String]

  /** Warning for a DynamicSelect(..) value */
  def dynamicSelectWarning(propertyName:String = "") = s"DynamicSelected-Value '$propertyName' has no effect."
  /** Warning for a conditional-Value.
    * E.G.: if condition then 0 else 5
    * */
  def conditionWarning(propertyName:String = "") = s"Conditional-Value '$propertyName' has no effect."

  // regex from: http://stackoverflow.com/a/5954831
  override val whiteSpace = """(\s|//.*|(?m)/\*(\*(?!/)|[^*])*\*/)+""".r
  protected val identRegex = """[a-zA-Z_][a-zA-Z0-9_\\.\(\)\[\]\{\}]*""".r
  protected val numberRegex = "-?[0-9]+".r
  protected val javaLikeStrRegex = "\"(.*)\"".r

  /** Wraps the given value in a StringValidation */
  def validValue[A](v:A): StringValidation[A] = Validation[A, String](v)

  @annotation.tailrec
  private def containsDuplicates[A](xs:List[A], seen:Set[A] = Set[A]()): Boolean = xs match {
    case hd::tl if seen.contains(hd) => true
    case hd::tl => containsDuplicates(tl, seen + hd)
    case Nil => false
  }

  /** Parses fields of an IconPrimitive with the given parsers and returns a map of parsed strings.
    * '''This method doesn't convert the value... The value isn't itself touched!'''
    *
    *  If there are dublicate fields this method will throw a ParsingError.
    * @param parsers The parsers for converting the fields of the IconPrimitive into a Tupel (key -> value).
    *                The parsers will be joined with the alternative composition ("|")
    * @return
    */
  def properties(parsers:Parser[(String, String)]*):Parser[Map[String,String]] = {
    val oredParser = parsers.tail.foldLeft(parsers.head)(_|_)

    val propertiesListParser = repsep(oredParser, ",")
    propertiesListParser.map { tuples =>
      if(containsDuplicates(tuples)) throw new ParsingError("Duplicate definitions of properties!")
      else tuples.toMap
    }
  }

  /** Turns the given keys into a parser for Tupels (key -> value) and uses this parser as input for properties()
    * returning properties() value. */
  def propertyKeys(keys:String*):Parser[Map[String,String]] = properties(keys.map(property):_*)

  def property(key:String):Parser[(String,String)] =
    (key <~ "=") ~ value ^^ { case k ~ v => (k,v) }

  /** Returns a value from the given '''map''' converted with '''parser''' and '''identified by key'''.
    * Returns default if there is no value for the given key.
    */
  def getPropertyValue[A](map:Map[String,String], key:String, default: => A)(parser:Parser[A]): A =
    map.get(key).map(parse(parser,_)).map {
      case Success(v,_) => v
      case NoSuccess(msg,_) =>
        throw new ParsingError(msg)
    }.getOrElse(default)

  /** Returns a value from the given '''map''' converted with '''parser''' and '''identified by key'''.
    * Throws a ParsingError if there is no value for the given key.
    */
    def getPropertyValue[A](map:Map[String,String], key:String)(parser:Parser[A]): A =
      getPropertyValue(map, key, throw new ParsingError(
          s"""property "$key" has to be defined!"""))(parser)

  val value:Parser[String] = (
    "DynamicSelect" ~ "(" ~ value ~ "," ~ value ~ ")" ^^ {
      case ds~lp~v~k~v2~rp => ds+lp+v+k+v2+rp
    }
    | "if" ~ identRegex ~ "then" ~ value ~ "else" ~ value ^^ {
      case ifs~id~th~vl~el~vl2 => s"$ifs $id $th $vl $el $vl2"
    }
    | identRegex
    | javaLikeStrRegex
    | numberRegex ~ "." ~ numberRegex ^^ { case n1~comma~n2 => n1+comma+n2 }
    | numberRegex
    | "{" ~ repsep(numberRegex|identRegex, ",") ~ "}" ^^ { case lp~inner~rp => lp+inner.mkString(",")+rp }
    | "{" ~ repsep(value, ",") ~ "}" ^^ { case lp~inner~rp => lp+inner.mkString(",")+rp }
  )

  def dynamicSelectedValue[A](v:Parser[A]): Parser[A] =
    "DynamicSelect" ~> "(" ~> v <~ "," <~ v <~ ")"

  def conditionValue[A](v:Parser[A]): Parser[A] =
    "if" ~> identRegex ~> "then" ~> v <~ "else" <~ v

  /** Parses a value which could be a variable-value.
    * E.G.: DynamicSelect(...), if condition then ... else ..
    */
  def withVariableValues[A](p:Parser[A], propertyName:String):Parser[StringValidation[A]] = (
      dynamicSelectedValue(p) ^^ { v => ValidationWarning(v, dynamicSelectWarning(propertyName))  }
    | conditionValue(p) ^^ { v => ValidationWarning(v, conditionWarning(propertyName))  }
    | p ^^ { ValidationSuccess(_) }
  )

  def extension:Parser[StringValidation[(Point,Point)]] = withVariableValues (
    ("{"~> point <~ ",") ~ point <~ "}" ^^ { case p1 ~ p2 => (p1,p2) }
    , "extent")

  def point:Parser[Point] =
    ("{" ~> decimalNo <~ ",") ~ decimalNo <~ "}" ^^ {
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

  def emptySeqString:Parser[Seq[String]] = "{" ~> repsep(ident, ",") <~ "}"

  def numberParser:Parser[Double] = numberRegex ^^ { _.toDouble }
  val decimalNo:Parser[Double]
  val ident:Parser[String]
  val bool:Parser[Boolean] = (
    "true" ^^^ true
    | "false"  ^^^ false
  )

  val classSpecialization:Parser[String] = (
    "class" | "model" | "record" | "type" | "block" | "function" | "connector" | "package"
  )
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
  val preserveRatio = "preserveAspectRatio"
  val initScale = "initialScale"
  val textString = "textString"
  val fontSize = "fontSize"
  val fontName = "fontName"
  val textStyle = "textStyle"
  val textColor = "textColor"
  val hAlignment = "horizontalAlignment"

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
  val defaultCoordinateSystemSize = (100.0,100.0)
  val defaultPreserveRatio = true
  val defaultinitScale = 0.1
  val defaultFontSize = 12.0
  val defaultFont = "Arial"
  val defaultFontStyle = Seq[String]()
  val defaultHAlignment = "TextAlignment.Center"
}
