package de.thm.move.controllers

import java.net.URL
import java.util.ResourceBundle
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.{Initializable, FXML}
import javafx.scene.Cursor
import javafx.scene.canvas.{GraphicsContext, Canvas}
import javafx.scene.control._
import javafx.scene.image.Image
import javafx.scene.input.{InputEvent, MouseEvent}
import javafx.scene.layout.{StackPane, HBox, Pane}
import javafx.scene.paint.Color
import javafx.scene.shape.{Rectangle, Shape}
import javafx.stage.FileChooser
import de.thm.move.views.DrawPanel

import collection.JavaConversions._

import de.thm.move.models.SelectedShape
import de.thm.move.models.CommonTypes._
import de.thm.move.models.SelectedShape.SelectedShape
import de.thm.move.views.Anchor
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
  var showAnchorsCheckbox: CheckBox = _

  @FXML
  var drawStub: StackPane = _
  private val drawPanel = new DrawPanel(shapeInputHandler)
  private val drawCtrl = new DrawCtrl(drawPanel)

  private val moveHandler = drawCtrl.getMoveHandler

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

    val handler = drawCtrl.getDrawHandler
    val drawHandler = { mouseEvent:MouseEvent =>
      selectedShape match {
        case Some(shape) =>  handler(shape, mouseEvent)(getFillColor, getStrokeColor, selectedThickness)
        case _ => //ignore
      }
    }

    drawPanel.setOnMousePressed(drawHandler)
    drawPanel.setOnMouseClicked(drawHandler)
    drawPanel.setOnMouseReleased(drawHandler)

  }

  def shapeInputHandler(ev:InputEvent): Unit = {
    if(selectedShape.isEmpty) {
      ev match {
        case mv: MouseEvent => moveHandler(mv)
      }
    }
  }

  @FXML
  def onLoadBitmap(e:ActionEvent): Unit = {
    val chooser = new FileChooser()
    chooser.setTitle("Open bitmap")
    val fileOp = Option(chooser.showOpenDialog(getWindow))
    fileOp map { file =>
      new Image(file.toURI.toString)
    } foreach {
      drawPanel.drawImage
    }
  }

  @FXML
  def onShowAnchorsClicked(e:ActionEvent): Unit = drawCtrl.setVisibilityOfAnchors(showAnchorsSelected)

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
  private def getWindow = drawPanel.getScene.getWindow
  private def showAnchorsSelected: Boolean = showAnchorsCheckbox.isSelected

  private def selectedShape: Option[SelectedShape] = {
    val btn = btnGroup.getSelectedToggle.asInstanceOf[ToggleButton]
    Option(btn.getId).flatMap(shapeBtnsToSelectedShapes.get(_))
  }
}