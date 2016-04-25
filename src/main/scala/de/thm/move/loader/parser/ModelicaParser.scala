/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.loader.parser

import javafx.scene.paint.Color

import de.thm.move.loader.parser.ast._
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

import scala.util.parsing.input.Position

class ModelicaParser extends JavaTokenParsers
  with ImplicitConversions
  with ModelicaParserLike
  with PropertyParser {
  val decimalNo = floatingPointNumber ^^ { _.toDouble }

  override val ident:Parser[String] = identRegex

  val start = model +
  def model:Parser[Model] = stuffBeforeModel ~> positioned(
    ("model" ~> identRegex) ~ moSource ~ posString("end") ~ identRegex <~ ";" ^^ {
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
    skipAnnotation ~> "annotation" ~> "(" ~> skipUninterestingStuff ~> posString(")") <~ ";" ^^ {
      paren => Some(WithoutIcon(paren.pos))
    }
    | skipUninterestingStuff ~> icon <~ ")" <~ ";" ^^ { Some(_) }
    | stuffAfterModel ^^ { _ => None }
  )

  def skipAnnotation = ((not("annotation") ~> ident ~> """([^\n]+)""".r) *)

  /** This parser skips everything that doesn't start with Icon because we are only intersted in Icon(.. */
  def skipUninterestingStuff = ((not("Icon") ~> ident ~> """([^\n]+)""".r) *)
  def stuffBeforeModel = ((not("model") ~> ident ~> """([^\n]+)""".r) *)
  def stuffAfterModel = ((not("end") ~> ident ~> """([^\n]+)""".r) *)

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
    CoordinateSystem(ext,aspectRatio,scale)
  })

  def extensionParser:Parser[Extent] =
    "extent" ~> "=" ~> extension

  def graphic:Parser[List[ShapeElement]] =
    "graphics" ~>  "=" ~> "{" ~> repsep(graphics, ",") <~ "}"

  def graphics:Parser[ShapeElement] = positioned(
    "Rectangle" ~> "(" ~> rectangleFields <~ ")"
    | "Ellipse" ~> "(" ~> ellipseFields <~ ")"
    | "Line" ~> "(" ~> lineFields <~ ")"
    | "Polygon" ~> "(" ~> polygonFields <~ ")"
    | "Bitmap" ~> "(" ~> bitmapFields <~ ")"
    )

  def rectangleFields:Parser[RectangleElement] =
    positioned(propertyKeys(visible, origin, rotation,lineCol,linePatt,fillCol,
      fillPatt,extent,lineThick, radius) ^^ { map =>
        val gi = getGraphicItem(map)
        val fs = getFilledShape(map)
        val ext = getPropertyValue(map, extent)(extension)
        RectangleElement(gi,fs, extent=ext)
    })

  def polygonFields:Parser[Polygon] =
    positioned(propertyKeys(visible, origin, rotation,pointsKey,lineCol,linePatt,fillCol,
      fillPatt,lineThick,smooth) ^^ { map =>
        Polygon(getGraphicItem(map),
        getFilledShape(map),
        getPropertyValue(map, pointsKey)(points),
        getPropertyValue(map, smooth, defaultSmooth)(ident)
        )
      })


  def lineFields:Parser[PathElement] =
    positioned(propertyKeys(visible,origin,rotation,pointsKey,colorKey,linePatt,thick,arrowKey,smooth) ^^ {
      map =>
        PathElement(getGraphicItem(map),
                    getPropertyValue(map, pointsKey)(points),
                    getPropertyValue(map, colorKey, defaultCol)(color),
                    getPropertyValue(map, thick, defaultLineThick)(numberParser),
                    getPropertyValue(map, linePatt, defaultLinePatt)(ident),
                    getPropertyValue(map, smooth, defaultSmooth)(ident),
                    getPropertyValue(map, arrowKey, defaultArrow)(arrow),
                    getPropertyValue(map, arrowSize, defaultArrowSize)(numberParser)
                  )

    })

  def ellipseFields:Parser[Ellipse] =
    positioned(propertyKeys(visible,origin,rotation,lineCol,linePatt,fillCol,
      fillPatt,extent,lineThick, endAngle) ^^ { map =>
        val gi = getGraphicItem(map)
        val fs = getFilledShape(map)
        val ext = getPropertyValue(map, extent)(extension)
        val endAng = getPropertyValue(map, endAngle, defaultEndAngle)(numberParser)
        Ellipse(gi,fs, extent=ext, endAngle = endAng)
      })


  def bitmapFields:Parser[AbstractImage] =
    positioned(propertyKeys(visible, origin, extent, rotation, base64, imgUri) ^^ { map =>
      val gi = getGraphicItem(map)
      val ext = getPropertyValue(map, extent)(extension)
      val base64Opt = map.get(base64).map(identWithoutHyphens)
      val imgUriOpt = map.get(imgUri).map(identWithoutHyphens)

      base64Opt.map(ImageBase64(gi,ext,_)).orElse(
        imgUriOpt.map(ImageURI(gi, ext, _))).getOrElse(
          throw new ParsingError("fileName or imageSource has to be defined for Bitmaps!")
          )
    })

  def getGraphicItem(map:Map[String,String]):GraphicItem = {
    GraphicItem(getPropertyValue(map, visible, defaultVisible)(bool),
                getPropertyValue(map, origin, defaultOrigin)(point),
                getPropertyValue(map, rotation, defaultRotation)(numberParser)
                )
  }

  def getFilledShape(map:Map[String,String]):FilledShape =
    FilledShape(getPropertyValue(map, fillCol, defaultCol)(color),
                getPropertyValue(map, fillPatt, defaultFillPatt)(ident),
                getPropertyValue(map, lineCol, defaultCol)(color),
                getPropertyValue(map, lineThick, defaultLineThick)(numberParser),
                getPropertyValue(map, linePatt, defaultLinePatt)(ident))

  def identWithoutHyphens(str:String):String = {
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
}
