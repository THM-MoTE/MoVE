package de.thm.move.views

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.value.{ObservableValue}
import javafx.event.{EventHandler}
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape._
import javafx.scene.image.{ImageView, Image}
import javafx.scene.input.{InputEvent}
import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.models.CommonTypes._
import javafx.beans.binding.Bindings

import de.thm.move.views.shapes.{ResizableLine, ResizableRectangle}

class DrawPanel(inputEventHandler:InputEvent => Unit) extends Pane {
  private var shapes = List[Shape]()

  this.setMaxWidth(Double.MaxValue)
  this.setMaxHeight(Double.MaxValue)

  def drawImage(img:Image) = {
    val view = new ImageView(img)
    view.setPreserveRatio(true)
    view.setFitWidth(200)
    super.getChildren.add(view)
  }

  def drawShape(s:Shape):Unit = {
    s.addEventHandler(InputEvent.ANY, new EventHandler[InputEvent]() {
      override def handle(event: InputEvent): Unit = inputEventHandler(event)
    })

    super.getChildren.add(s)

    shapes = s :: shapes
  }

  def drawShapes(shapes:Shape*) = shapes.foreach(drawShape)

  private def colorizeShape(s:Shape, fColor:Color, strColor:Color):Unit = {
    s.setFill(fColor)
    s.setStroke(strColor)
  }

  def drawRectangle(point:Point, width:Double, height:Double)(fillColor:Color, strokeColor:Color):Unit = {

    val rectangle = new ResizableRectangle(point, width, height)
    rectangle.colorizeShape(fillColor, strokeColor)
    drawShapes(rectangle)
    drawShapes(rectangle.getAnchors:_*)
    /*
    val (x,y) = point

    val rectangle = new Rectangle(x,y,width,height)
    colorizeShape(rectangle, fillColor, strokeColor)

    //create resize anchors
    val topLeftAnchor = new Anchor(x,y, fillColor)
    val topRightAnchor = new Anchor(x+width,y, fillColor)
    val bottomLeftAnchor = new Anchor(x,y+height, fillColor)
    val bottomRightAnchor = new Anchor(x+width, y+height, fillColor)

    rectangle.xProperty().bind(topLeftAnchor.centerXProperty())
    rectangle.yProperty().bind(topLeftAnchor.centerYProperty())
    rectangle.widthProperty().bind(topLeftAnchor.centerXProperty().multiply(4))

    rectangle.widthProperty().bind(bottomRightAnchor.centerXProperty().subtract(x))
    rectangle.heightProperty().bind(bottomRightAnchor.centerYProperty().subtract(y))
 
    bindAnchorsTranslationToShapesLayout(rectangle)(topLeftAnchor, topRightAnchor, bottomLeftAnchor, bottomRightAnchor)

    drawShapes(rectangle, topLeftAnchor, topRightAnchor, bottomLeftAnchor, bottomRightAnchor)
    */
  }

  def drawLine(start:Point, end:Point, strokeSize:Int)(fillColor:Color, strokeColor:Color):Unit = {
    val line = new ResizableLine(start, end, strokeSize)
    line.colorizeShape(fillColor, strokeColor)
    drawShape(line)
    drawShapes(line.getAnchors:_*)

    /*val (startX, startY) = start
    val (endX, endY) = end
    val line = new Line(startX,startY, endX,endY)
    colorizeShape(line, fillColor, strokeColor)
    line.setStrokeWidth(strokeSize.toDouble)

    //create resize anchors
    val startAnchor = new Anchor(startX, startY, fillColor)
    val endAnchor = new Anchor(endX, endY, fillColor)

    line.startXProperty().bind(startAnchor.centerXProperty())
    line.startYProperty().bind(startAnchor.centerYProperty())
    line.endXProperty().bind(endAnchor.centerXProperty())
    line.endYProperty().bind(endAnchor.centerYProperty())

    bindAnchorsTranslationToShapesLayout(line)(startAnchor, endAnchor)

    drawShapes(line, startAnchor, endAnchor)*/
  }

  def drawCircle(point:Point, width:Double, height:Double)(fillColor:Color, strokeColor:Color):Unit = {
    val (x,y) = point
    val circle = new Ellipse(x,y, width, height)
    colorizeShape(circle, fillColor, strokeColor)

    val anchor = new Anchor(x,y, Color.WHITE)

    circle.radiusXProperty().bind(anchor.centerXProperty())
    circle.radiusYProperty().bind(anchor.centerYProperty())
    bindAnchorsTranslationToShapesLayout(circle)(anchor)
    drawShapes(circle, anchor)
  }

  def drawAnchor(point:Point)(fillColor:Color):Unit = {
    val (x,y) = point
    drawShape(new Anchor(x,y,fillColor))
  }

  def drawPolygon(points:List[Point])(fillColor:Color, strokeColor:Color):Unit = {
    val singlePoints = points.flatMap { case (x,y) => List(x,y) }
    val polygon = new Polygon(singlePoints:_*)
    colorizeShape(polygon, fillColor, strokeColor)
    removeDrawnAchors()

    //create drag-drop anchors
    val observablePoints = polygon.getPoints
    for(i <- 0 until observablePoints.size by 2) {
      val xIdx = i
      val yIdx = i+1
      val xProperty = new SimpleDoubleProperty(observablePoints.get(xIdx))
      val yProperty = new SimpleDoubleProperty(observablePoints.get(yIdx))

      xProperty.addListener({ (ov: ObservableValue[_ <: Number], oldX: Number, newX: Number) =>
        val _ = observablePoints.set(xIdx, newX.doubleValue())
      })
      yProperty.addListener({ (ov: ObservableValue[_ <: Number], oldX: Number, newX: Number) =>
        val _ = observablePoints.set(yIdx, newX.doubleValue())
      })

      val anchor = new Anchor(xProperty.get(), yProperty.get(), Color.RED)
      xProperty.bind(anchor.centerXProperty())
      yProperty.bind(anchor.centerYProperty())
      bindAnchorsTranslationToShapesLayout(polygon)(anchor)
      drawShape(anchor)
    }

    drawShape(polygon)
  }

  private def removeDrawnAchors():Unit = {
    println(shapes)
    //get indexes of drawn anchors
    val removingAnchors = shapes.takeWhile( _.isInstanceOf[Anchor] )
    //remove from shapelist
    shapes = shapes.dropWhile(_.isInstanceOf[Anchor])

    //remove from painting area
    this.getChildren.removeAll(removingAnchors:_*)
  }

  private def bindAnchorsTranslationToShapesLayout(shape:Shape)(anchors:Anchor*): Unit = {
    anchors.foreach { anchor =>
      anchor.layoutXProperty().bind(shape.layoutXProperty())
      anchor.layoutYProperty().bind(shape.layoutYProperty())
    }
  }
}
