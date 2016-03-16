package de.thm.move.controllers

import java.net.URL
import java.util.ResourceBundle
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.{Initializable, FXML}
import javafx.scene.Cursor
import javafx.scene.canvas.{GraphicsContext, Canvas}
import javafx.scene.control._
import javafx.scene.input.{InputEvent, MouseEvent}
import javafx.scene.layout.{StackPane, HBox, Pane}
import javafx.scene.paint.Color
import javafx.scene.shape.{Rectangle, Shape}
import de.thm.move.views.DrawPanel

import collection.JavaConversions._

import de.thm.move.models.SelectedShape
import de.thm.move.models.CommonTypes._
import de.thm.move.models.SelectedShape.SelectedShape
import implicits.FxHandlerImplicits._

class MoveCtrl extends Initializable {

  @FXML
  var btnGroup: ToggleGroup = _
  @FXML
  var fillColorPicker: ColorPicker = _
  @FXML
  var strokeColorPicker: ColorPicker = _

  @FXML
  var borderThicknessChooser: ChoiceBox[Int] = _

  @FXML
  var drawStub: StackPane = _
  private lazy val drawPanel = new DrawPanel(shapeInputHandler)

  private val shapeBtnsToSelectedShapes = Map(
      "rectangle_btn" -> SelectedShape.Rectangle,
      "circle_btn" -> SelectedShape.Circle,
      "line_btn" -> SelectedShape.Line,
      "polygon_btn" -> SelectedShape.Polygon
    )

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    drawStub.getChildren.add(drawPanel)
    fillColorPicker.setValue(Color.BLACK)
    strokeColorPicker.setValue(Color.BLACK)

    val sizesList:java.util.List[Int] = (1 until 20).toList
    borderThicknessChooser.setItems(FXCollections.observableArrayList(sizesList))
    borderThicknessChooser.getSelectionModel.selectFirst()

      var points = List[Point]()

    val drawHandler = { mouseEvent:MouseEvent =>
      selectedShape match {
        case Some(SelectedShape.Polygon) =>
          if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
            val newX = mouseEvent.getX()
            val newY = mouseEvent.getY()
            //test if polygon is finish by checking if last clicked position is already in the coordinate list
            points.find {
              case (x, y) => Math.abs(x - newX) <= 10 && Math.abs(y - newY) <= 10
            } match {
              case Some(_) =>
                //draw the polygon
                drawPanel.drawPolygon(points)(getFillColor, getStrokeColor)
                points = List()
              case None =>
                points = (newX, newY) :: points
                drawPanel.drawAnchor(points.head)(getFillColor)
            }
          }
        case Some(_) =>
          if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
            points = (mouseEvent.getX(), mouseEvent.getY()) :: points
          } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
            points = (mouseEvent.getX(), mouseEvent.getY()) :: points

            points match {
              case end::start::_ => drawCustomShape(start, end)
            }
            points = List()
          }
        case _ => //ignore
      }
    }

    drawPanel.setOnMousePressed(drawHandler)
    drawPanel.setOnMouseClicked(drawHandler)
    drawPanel.setOnMouseReleased(drawHandler)

  }

  private var orgSceneX = -1.0
  private var orgSceneY = -1.0
  private var orgTransX = -1.0
  private var orgTransY = -1.0

  def shapeInputHandler(ev:InputEvent): Unit = {
    if(selectedShape.isEmpty) {
      ev match {
        case mv: MouseEvent =>
          //move an element
          if (mv.getEventType == MouseEvent.MOUSE_PRESSED) {
            //save original coordinates
            orgSceneX = mv.getSceneX
            orgSceneY = mv.getSceneY
            mv.getSource match {
              case s:Shape =>
                orgTransX = s.getTranslateX
                orgTransY = s.getTranslateY
              case _ => throw new IllegalStateException("shapeInputHandler: source isn't a shape")
            }
          } else if (mv.getEventType == MouseEvent.MOUSE_DRAGGED) {
            //translate from original to new position
            val offsetX = mv.getSceneX() - orgSceneX
            val offsetY = mv.getSceneY() - orgSceneY
            val newX = orgTransX + offsetX
            val newY = orgTransY + offsetY
            mv.getSource match {
              case s:Shape =>
               s.setTranslateX(newX)
               s.setTranslateY(newY)
              case _ => throw new IllegalStateException("shapeInputHandler: source isn't a shape")
            }
          }
      }
    }
  }

  private def drawAnchor(x:Point):Unit = drawPanel.drawAnchor(x)(getFillColor)

  @FXML
  def onPointerClicked(e:ActionEvent): Unit = changeDrawingCursor(Cursor.DEFAULT)
  @FXML
  def onCircleClicked(e:ActionEvent): Unit = changeDrawingCursor(Cursor.CROSSHAIR)
  @FXML
  def onRectangleClicked(e:ActionEvent): Unit = changeDrawingCursor(Cursor.CROSSHAIR)
  @FXML
  def onLineClicked(e:ActionEvent): Unit = changeDrawingCursor(Cursor.CROSSHAIR)
  @FXML
  def onPolygonClicked(e:ActionEvent): Unit = changeDrawingCursor(Cursor.CROSSHAIR)

  private def getStrokeColor: Color = strokeColorPicker.getValue
  private def getFillColor: Color = fillColorPicker.getValue
  private def selectedThickness: Int = borderThicknessChooser.getSelectionModel.getSelectedItem
  private def changeDrawingCursor(c:Cursor): Unit = drawPanel.setCursor(c)

  private def selectedShape: Option[SelectedShape] = {
    val btn = btnGroup.getSelectedToggle.asInstanceOf[ToggleButton]
    Option(btn.getId).flatMap(shapeBtnsToSelectedShapes.get(_))
  }

  private def drawCustomShape(start:Point, end:Point) = {
      val (startX, startY) = start
      val (endX, endY) = end

      selectedShape.foreach {
        case SelectedShape.Rectangle =>
          val width = endX - startX
          val height = endY - startY
          drawPanel.drawRectangle(start, width, height)(getFillColor, getStrokeColor)
        case SelectedShape.Polygon =>
        case SelectedShape.Circle =>
          val width = endX - startX
          val height = endY - startY
          drawPanel.drawCircle(start, width, height)(getFillColor, getStrokeColor)
        case SelectedShape.Line =>
          val thickness = borderThicknessChooser.getSelectionModel.getSelectedItem
          drawPanel.drawLine(start, end, selectedThickness)(getFillColor, getStrokeColor)
      }
  }
}