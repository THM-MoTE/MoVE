package de.thm.move.loader.parser

import javafx.scene.paint.Color

import de.thm.move.models.CommonTypes._

sealed trait ModelicaAst
case class Model(name:String, annotations:List[Annotation]) extends ModelicaAst
sealed trait Annotation extends ModelicaAst

case class Icon(coordinationSystem:Option[CoordinateSystem], grapchics:List[ShapeElement]) extends Annotation
case class CoordinateSystem(extension:(Point,Point)) extends ModelicaAst

abstract class ShapeElement() extends ModelicaAst
abstract class ColoredShapeElement(fillColor:Color,
                                   fillPattern: String,
                                   strokeColor:Color,
                                   strokePattern: String) extends ShapeElement
case class RectangleElement(start:Point,
                            width:Double,
                            height:Double,
                            fillColor:Color,
                            fillPattern: String,
                            strokeColor:Color,
                            strokePattern: String
                            ) extends ColoredShapeElement(fillColor, fillPattern, strokeColor, strokePattern)