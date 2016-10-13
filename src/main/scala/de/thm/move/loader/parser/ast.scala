/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.loader.parser

import javafx.scene.paint.Color

import de.thm.move.models.CommonTypes._

import scala.util.parsing.input.{Position, Positional}

object ast {
  type Extent = (Point,Point)

  case class PositionedString(s:String) extends Positional
  case class IconElements(coordinationSystem:Option[CoordinateSystem],
                          grapchics:List[ShapeElement],
                          end:Position) extends Positional

  sealed trait ModelicaAst extends Positional
  case class Model(name:String, annot:Annotation) extends ModelicaAst

  sealed trait Annotation extends ModelicaAst
  /** There is no annotation, but it would be at pos! */
  case class NoAnnotation(position:Position) extends Annotation
  /** There is a annotation but without a icon, icon would be at position pos! */
  case class WithoutIcon(position:Position) extends Annotation
  case class Icon(coordinationSystem:Option[CoordinateSystem],
                  grapchics:List[ShapeElement],
                  start:Position,
                  end:Position) extends Annotation
  case class CoordinateSystem(extension:Extent,
                        preserveRatio:Boolean = true,
                        initScale:Double = 0.1
                        ) extends ModelicaAst

  sealed trait ShapeElement extends ModelicaAst {
    /** A list of possible warnings that occured during parsing the file.
      * Will be used to warn the user when some Property can't get used
      */
    var warnings: List[String] = Nil
  }

  case class GraphicItem(visible:Boolean = true,
                        origin:Point = (0,0),
                        rotation: Double = 0.0
                        )
  case class FilledShape(fillColor:Color = Color.BLACK,
                         fillPattern: String = "FillPattern.None",
                         strokeColor:Color = Color.BLACK,
                         strokeSize: Double = 1.0,
                         strokePattern: String = "LinePattern.Solid")

  case class RectangleElement(gItem:GraphicItem,
                              filledShape:FilledShape,
                              borderPattern:String = "BorderPattern.None",
                              extent:Extent,
                              radius:Double = 0.0
                              ) extends ShapeElement

  abstract class PathLikeElement(points:List[Point]) extends ShapeElement

  case class PathElement(gItem:GraphicItem,
                        points:List[Point],
                        color:Color = Color.BLACK,
                        strokeSize: Double = 1.0,
                        strokePattern: String = "LinePattern.Solid",
                        smooth:String = "Smooth.None",
                        arrow:(String,String) = ("Arrow.None", "Arrow.None"),
                        arrowSize:Double = 3.0
                      ) extends PathLikeElement(points)

  case class Polygon(gItem:GraphicItem,
                    filledShape:FilledShape,
                    points:List[Point],
                    smooth:String = "Smooth.None"
                  ) extends PathLikeElement(points)

  case class Ellipse(gItem:GraphicItem,
                    filledShape:FilledShape,
                    extent:Extent,
                    startAngle:Double = 0.0,
                    endAngle:Double = 360.0
                    ) extends ShapeElement

  abstract class AbstractImage(gItem:GraphicItem,
                              extent:Extent) extends ShapeElement
  case class ImageURI(gItem:GraphicItem,
                      extent:Extent,
                      fileName:String) extends AbstractImage(gItem, extent)

  case class ImageBase64(gItem:GraphicItem,
                          extent:Extent,
                          imageSource:String
                          ) extends AbstractImage(gItem, extent)
  case class Text(gItem:GraphicItem,
                  extent:Extent,
                  text:String,
                  size:Double,
                  fontName:String,
                  style: Seq[String],
                  color: Color,
                  hAlignment: String
                  ) extends ShapeElement
}
