/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.loader.parser

import java.io.{BufferedInputStream, InputStream, InputStreamReader}

import de.thm.move.loader.parser.ModelicaParserLike.ParsingError
import de.thm.move.loader.parser.PropertyParser._
import de.thm.move.loader.parser.ast._
import de.thm.move.util.ValidationSuccess

import scala.language.postfixOps
import scala.util._
import scala.util.parsing.combinator.{ImplicitConversions, JavaTokenParsers}

class ModelicaParser extends JavaTokenParsers
  with ImplicitConversions
  with ModelicaParserLike
  with PropertyParser {
  val decimalNo = floatingPointNumber ^^ { _.toDouble }
  val nonWhitespaceRegex = """[^\s]+""".r

  override val ident:Parser[String] = identRegex

  val start = model +
  def model:Parser[Model] = stuffBeforeModel ~> positioned(
    (classSpecialization ~> identRegex) ~ (classComment ~> moSource) ~ posString("end") ~ identRegex <~ ";" ^^ {
      case startIdent ~ icon ~ endPos ~ endIdent =>
        if(startIdent == endIdent) {
          Model(startIdent, icon getOrElse {
            NoAnnotation(endPos.pos)
          })
        }
        else throw new ParsingError(s"Modelname at end of file doesn't match starting modelname! ($startIdent != $endIdent)")
    })

  def posString(s:String) = positioned(s ^^ PositionedString)

  /** Icon is optional; there are models without a icon */
  def moSource:Parser[Option[Annotation]] = (
    skipAnnotation ~> "annotation" ~> "(" ~> icon <~ ")" <~";" ^^ { Some(_) }
    | skipAnnotation ~> "annotation" ~> "(" ~> skipUninterestingStuff ~> posString(")") <~ ";" ^^ {
      paren => Some(WithoutIcon(paren.pos))
    }
    | stuffAfterModel ^^ { _ => None }
  )


  /** The skip parsers are working like this:
    * 1. check that the word isn't the word we are searching (e.g.: annotation)
    * 2. if it's not, skip it (everythign that's not whiespace as defined by
    *  noneWhitespaceRegex) and also skip till a linebreak (\n)
    *  if the word is the searched word, this parser failes/stops
    */

  def skipAnnotation = ((not("annotation" ~> "(") ~> nonWhitespaceRegex ~> """([^\n]+)""".r) *)

  /** This parser skips everything that doesn't start with Icon because we are only intersted in Icon(.. */
  def skipUninterestingStuff = ((not("Icon") ~> nonWhitespaceRegex ~> """([^\n]+)""".r) *)
  def stuffBeforeModel = ((not(classSpecialization) ~> nonWhitespaceRegex ~> """([^\n]+)""".r) *)
  def stuffAfterModel = ((not("end") ~> nonWhitespaceRegex ~> """([^\n]+)""".r) *)

  def classComment:Parser[Option[String]] =
    "\"" ~> """([^"]*)""".r <~ "\"" ^^ { s => s } ?

  def annotation:Parser[List[Annotation]] = "annotation" ~> "(" ~> annotations <~ ")" <~ ";"
  def annotations:Parser[List[Annotation]] =
    icon +

  def icon:Parser[Icon] =
    iconElements ^^ { elems =>
      Icon(elems.coordinationSystem,
        elems.grapchics,
        elems.pos,
        elems.end
      )
    }

  def iconElements:Parser[IconElements] =
    positioned(("Icon" <~ "(") ~>
      ((coordinateSys <~ ",") ?) ~
      graphic ~
      posString(")") ^^ { case sys ~ graphic ~ endStr =>
      IconElements(sys, graphic, endStr.pos)
    })

  def coordinateSys: Parser[CoordinateSystem] =
    positioned("coordinateSystem" ~>"(" ~> coordinateSysFields <~ ")")

  def coordinateSysFields:Parser[CoordinateSystem] =
    positioned(propertyKeys(extent, preserveRatio, initScale) ^^ { map =>
    val ext = getPropertyValue(map, extent)(extension)
    val aspectRatio = getPropertyValue(map, preserveRatio, defaultPreserveRatio)(bool)
    val scale = getPropertyValue(map, initScale, defaultinitScale)(decimalNo)
    ext match {
      case ValidationSuccess(et) => CoordinateSystem(et,aspectRatio,scale)
      case _ => throw new ParsingError("extension for coordinate system has to be statically defined!")
    }
  })

  def extensionParser:Parser[StringValidation[Extent]] =
    "extent" ~> "=" ~> extension

  def graphic:Parser[List[ShapeElement]] =
    "graphics" ~>  "=" ~> "{" ~> repsep(graphics, ",") <~ "}"

  def graphics:Parser[ShapeElement] = positioned (
    "Rectangle" ~> "(" ~> rectangleFields <~ ")"
    | "Ellipse" ~> "(" ~> ellipseFields <~ ")"
    | "Line" ~> "(" ~> lineFields <~ ")"
    | "Polygon" ~> "(" ~> polygonFields <~ ")"
        | "Bitmap" ~> "(" ~> bitmapFields <~ ")"
        | "Text" ~> "(" ~> textFields <~ ")"
    )

  def rectangleFields:Parser[RectangleElement] =
    positioned(propertyKeys(visible, origin, rotation,lineCol,linePatt,fillCol,
      fillPatt,extent,lineThick, radius) ^^ { map =>
        val withWarnings = for {
          gi <- getGraphicItem(map)
          fs <- getFilledShape(map)
          ext <- getPropertyValue(map, extent)(extension)
        } yield RectangleElement(gi,fs, extent=ext)
        toAst(withWarnings)
    })

  def polygonFields:Parser[Polygon] =
    positioned(propertyKeys(visible, origin, rotation,pointsKey,lineCol,linePatt,fillCol,
      fillPatt,lineThick,smooth) ^^ { map =>
      toAst(for {
        gi <- getGraphicItem(map)
        fs <- getFilledShape(map)
        points <- getPropertyValue(map, pointsKey)(withVariableValues(points, pointsKey))
        smooth <- getPropertyValue(map, smooth, validValue(defaultSmooth))(withVariableValues(ident, smooth))
      } yield Polygon(gi,fs,points,smooth))
      })

  def lineFields:Parser[PathElement] =
    positioned(propertyKeys(visible,origin,rotation,pointsKey,colorKey,linePatt,thick,arrowKey,smooth) ^^ {
      map =>
        toAst(for {
          gi <- getGraphicItem(map)
          points <- getPropertyValue(map, pointsKey)(withVariableValues(points, pointsKey))
          col <- getPropertyValue(map, colorKey, validValue(defaultCol))(withVariableValues(color, colorKey))
          thick <- getPropertyValue(map, thick, validValue(defaultLineThick))(withVariableValues(decimalNo, thick))
          lp <- getPropertyValue(map, linePatt, validValue(defaultLinePatt))(withVariableValues(ident, linePatt))
          smooth <- getPropertyValue(map, smooth, validValue(defaultSmooth))(withVariableValues(ident,  smooth))
          arrow <- getPropertyValue(map, arrowKey, validValue(defaultArrow))(withVariableValues(arrow, arrowKey))
          as <- getPropertyValue(map, arrowSize, validValue(defaultArrowSize))(withVariableValues(numberParser, arrowSize))
        } yield PathElement(gi, points, col, thick, lp, smooth, arrow, as))
    })

  def ellipseFields:Parser[Ellipse] =
    positioned(propertyKeys(visible,origin,rotation,lineCol,linePatt,fillCol,
      fillPatt,extent,lineThick, endAngle) ^^ { map =>
        toAst( for {
          gi <- getGraphicItem(map)
          fs <- getFilledShape(map)
          ext <- getPropertyValue(map, extent)(extension)
          endAngl <- getPropertyValue(map, endAngle, validValue(defaultEndAngle))(withVariableValues(numberParser, endAngle))
        } yield  Ellipse(gi,fs, extent=ext, endAngle = endAngl) )
      })


  def bitmapFields:Parser[AbstractImage] =
    positioned(propertyKeys(visible, origin, extent, rotation, base64, imgUri) ^^ { map =>
      toAst(for {
        gi <- getGraphicItem(map)
        ext <- getPropertyValue(map, extent)(extension)
        base64Opt = map.get(base64).map(stringLiteral)
        imgUriOpt = map.get(imgUri).map(stringLiteral)
      } yield {
        base64Opt.map(ImageBase64(gi, ext, _)).orElse(
          imgUriOpt.map(ImageURI(gi, ext, _))).getOrElse(
          missingKeyError("fileName or imageSource has to be defined for Bitmaps!"))
      })
    })

  def textFields:Parser[Text] =
    positioned(propertyKeys(visible, origin, extent, rotation, textString,
      fontSize,fontName,textStyle,textColor,hAlignment) ^^ { map =>
        toAst( for {
          gi <- getGraphicItem(map)
          ext <- getPropertyValue(map, extent)(extension)
          text = map.get(textString).map(stringLiteral).getOrElse(missingKeyError(textString))
          fs <- getPropertyValue(map, fontSize, validValue(defaultFontSize))(withVariableValues(decimalNo, fontSize))
          font = map.get(fontName).map(stringLiteral).getOrElse(defaultFont)
          fontStyle <- getPropertyValue(map, textStyle, validValue(defaultFontStyle))(withVariableValues(emptySeqString, textStyle))
          cl <- getPropertyValue(map, textColor, validValue(defaultCol))(withVariableValues(color, textColor))
          halignment <- getPropertyValue(map, hAlignment, validValue(defaultHAlignment))(withVariableValues(ident, hAlignment))
        } yield Text(gi,ext,text,fs,font,fontStyle, cl, halignment))
    })

  def getGraphicItem(map:Map[String,String]):StringValidation[GraphicItem] = {
    for {
      visible <- getPropertyValue(map, visible, validValue(defaultVisible))(withVariableValues(bool, visible))
      origin <- getPropertyValue(map, origin, validValue(defaultOrigin))(withVariableValues(point, origin))
      rotation <- getPropertyValue(map, rotation, validValue(defaultRotation))(withVariableValues(numberParser, rotation))
    } yield GraphicItem(visible,origin,rotation)
  }

  def getFilledShape(map:Map[String,String]):StringValidation[FilledShape] =
    for {
      cl <- getPropertyValue(map, fillCol, validValue(defaultCol))(withVariableValues(color, fillCol))
      fp <- getPropertyValue(map, fillPatt, validValue(defaultFillPatt))(withVariableValues(ident, fillPatt))
      lc <- getPropertyValue(map, lineCol, validValue(defaultCol))(withVariableValues(color, lineCol))
      thick <- getPropertyValue(map, lineThick, validValue(defaultLineThick))(withVariableValues(decimalNo, lineThick))
      lp <- getPropertyValue(map, linePatt, validValue(defaultLinePatt))(withVariableValues(ident, linePatt))
    } yield FilledShape(cl,fp,lc,thick,lp)

  def stringLiteral(str:String):String = {
    val regex = "\"(.*)\"".r
    str match {
      case regex(inner) => inner
      case _ =>  throw new ParsingError(s"$str doesn't match $regex")
    }
  }

  def parse(stream:InputStream): Try[List[Model]] = Try {
    parseAll(start, new InputStreamReader(new BufferedInputStream(stream))) match {
      case Success(result, _)    => result
      case NoSuccess(msg, input) =>
        throw new ParsingError(s"Error in line ${input.pos.line}, column ${input.pos.column}: "+msg)
    }
  }

  private def toAst[A <: ShapeElement](parsedWarnings:StringValidation[A]):A = {
    val v = parsedWarnings.getValue
    v.warnings = parsedWarnings.getWarnings
    v
  }

  private def missingKeyError(str:String) = throw new ParsingError(s"$str has to be defined!")
}
