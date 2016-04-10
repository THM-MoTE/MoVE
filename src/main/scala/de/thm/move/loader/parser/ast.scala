package de.thm.move.loader.parser

import javafx.scene.paint.Color

import de.thm.move.models.CommonTypes._

object ast {
  type Extent = (Point,Point)

  sealed trait ModelicaAst
  case class Model(name:String, annotations:List[Annotation]) extends ModelicaAst
  sealed trait Annotation extends ModelicaAst

  case class Icon(coordinationSystem:Option[CoordinateSystem],
                  grapchics:List[ShapeElement]) extends Annotation
  case class CoordinateSystem(extension:Extent) extends ModelicaAst

  sealed trait ShapeElement extends ModelicaAst

  case class Rotation(quantity:String="angle",
                      unit:String="deg",
                      deg:Double=0.0)
  case class GraphicItem(visible:Boolean = true,
                        origin:Point = (0,0),
                        rotation: Rotation = Rotation()
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
                    startAngle:Rotation = Rotation(),
                    endAngle:Rotation = Rotation()
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
}
