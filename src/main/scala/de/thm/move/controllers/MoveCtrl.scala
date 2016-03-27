/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.controllers

import java.net.URL
import java.util.ResourceBundle
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.{Initializable, FXML}
import javafx.scene.Cursor
import javafx.scene.canvas.{GraphicsContext, Canvas}
import javafx.scene.control._
import javafx.scene.image.Image
import javafx.scene.input.{KeyCode, KeyEvent, InputEvent, MouseEvent}
import javafx.scene.layout.{StackPane, HBox, Pane}
import javafx.scene.paint.Color
import javafx.scene.shape.{Rectangle, Shape}
import javafx.stage.FileChooser
import de.thm.move.Global
import de.thm.move.views.DrawPanel
import de.thm.move.views.shapes.ResizableShape

import collection.JavaConversions._

import de.thm.move.models.SelectedShape
import de.thm.move.models.CommonTypes._
import de.thm.move.models.SelectedShape.SelectedShape
import de.thm.move.views.Anchor
import implicits.FxHandlerImplicits._

class MoveCtrl extends Initializable {

  @FXML
  var undoMenuItem: MenuItem = _
  @FXML
  var redoMenuItem: MenuItem = _
  @FXML
  var loadImgMenuItem: MenuItem = _

  @FXML
  var showAnchorsItem: CheckMenuItem = _

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
  private val drawPanel = new DrawPanel()
  private val drawCtrl = new DrawCtrl(drawPanel, shapeInputHandler)

  private val moveHandler = drawCtrl.getMoveHandler

  private val shapeBtnsToSelectedShapes = Map(
      "rectangle_btn" -> SelectedShape.Rectangle,
      "circle_btn" -> SelectedShape.Circle,
      "line_btn" -> SelectedShape.Line,
      "polygon_btn" -> SelectedShape.Polygon
    )

  /**
   * Maps registered KeyCodes (from shortcuts.conf) to corresponding button
    * '''! Ensure that this field is initialized AFTER all fields are initiazlized !'''
    */
  private lazy val keyCodeToButtons = {
    val buttons = btnGroup.getToggles.map(_.asInstanceOf[ToggleButton])
    def getButtonById(id:String): Option[ToggleButton] = {
      buttons.find(_.getId == id)
    }

    val keyCodeOpts = List(
      Global.shortcuts.getKeyCode("move-elements") -> getButtonById("line_pointer"),
      Global.shortcuts.getKeyCode("draw-rectangle") -> getButtonById("rectangle_btn"),
      Global.shortcuts.getKeyCode("draw-line") -> getButtonById("line_btn"),
      Global.shortcuts.getKeyCode("draw-polygon") -> getButtonById("polygon_btn"),
      Global.shortcuts.getKeyCode("draw-circle") -> getButtonById("circle_btn")
      )

    val codes = keyCodeOpts flatMap {
      case (Some(code),Some(btn)) => List( (code, btn) )
      case _ => Nil
    }

    codes.toMap
  }


    /*Setup given keyboard shortcuts to given item*/
    private def setupShortcuts(keys:String*)(menues:MenuItem*) = {
      for (
        (key, menu) <- keys zip menues
      ) {
        Global.shortcuts.getShortcut(key) foreach menu.setAccelerator
      }
    }

    /*Setup default colors for fill-,strokeChooser & strokeWidth*/
    private def setupDefaultColors(): Unit = {
      def asColor(key:String): Option[Color] =
        Global.config.getString(key).map(Color.web)

      val fillColor = asColor("colorChooser.fillColor").getOrElse(Color.BLACK)
      val strokeColor = asColor("colorChooser.strokeColor").getOrElse(Color.BLACK)
      val width = Global.config.getInt("colorChooser.strokeWidth").getOrElse(1)

      fillColorPicker.setValue(fillColor)
      strokeColorPicker.setValue(strokeColor)
      borderThicknessChooser.setValue(width)
    }

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    setupShortcuts("undo", "redo", "load-image", "show-anchors")(undoMenuItem, redoMenuItem, loadImgMenuItem, showAnchorsItem)
    drawStub.getChildren.add(drawPanel)

    val sizesList:java.util.List[Int] = (1 until 20).toList
    borderThicknessChooser.setItems(FXCollections.observableArrayList(sizesList))
    setupDefaultColors()

    val handler = drawCtrl.getDrawHandler

    val drawHandler = { mouseEvent:MouseEvent =>
      selectedShape match {
        case Some(shape) =>
          drawCtrl.removeSelectedShape
          handler(shape, mouseEvent)(getFillColor, getStrokeColor, selectedThickness)
        case _ => //ignore
      }
    }

    //add eventhandlers
    fillColorPicker.setOnAction(colorPickerChanged _)
    strokeColorPicker.setOnAction(colorPickerChanged _)

    borderThicknessChooser.getSelectionModel.
      selectedItemProperty.addListener { (_:Int, newX:Int) =>
        drawCtrl.setStrokeWidthForSelectedShape(newX)
      }

    drawPanel.setOnMousePressed(drawHandler)
    drawPanel.setOnMouseDragged(drawHandler)
    drawPanel.setOnMouseClicked(drawHandler)
    drawPanel.setOnMouseReleased(drawHandler)
  }

  /** Called after the scene is fully-constructed and displayed.
    * (Used for adding a key-event listener)
    */
  def setupMove(): Unit = {
    drawStub.getScene.setOnKeyPressed { ke: KeyEvent =>
      keyCodeToButtons.get(ke.getCode) foreach (_.fire)
    }
    drawStub.requestFocus()
  }

  def colorPickerChanged(ae:ActionEvent): Unit = {
    val src = ae.getSource
    if(src == strokeColorPicker)
      drawCtrl.setStrokeColorForSelectedShape(strokeColorPicker.getValue)
    else if(src == fillColorPicker)
      drawCtrl.setFillColorForSelectedShape(fillColorPicker.getValue)
  }

  def shapeInputHandler(ev:InputEvent): Unit = {
    if(selectedShape.isEmpty) {
      ev match {
        case mv:MouseEvent if mv.getEventType == MouseEvent.MOUSE_CLICKED =>
          mv.getSource() match {
            case s:ResizableShape => drawCtrl.setSelectedShape(s)
            case _:Anchor => //ignore can't change
          }

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
      drawCtrl.drawImage
    }
  }

  private def onDrawShape: Unit = {
    setDrawingCursor(Cursor.CROSSHAIR)
    drawCtrl.removeSelectedShape
  }

  @FXML
  def onPreserveRatioPressed(e:ActionEvent): Unit = {
    val src = e.getSource.asInstanceOf[CheckBox]

    drawPanel.getChildren.flatMap {
      case x:ResizableShape => List(x)
      case _ => Nil
    } foreach (_.resizeProportionalProperty.set(src.isSelected))
  }

  @FXML
  def onShowAnchorsClicked(e:ActionEvent): Unit = drawCtrl.setVisibilityOfAnchors(showAnchorsSelected)

  @FXML
  def onUndoClicked(e:ActionEvent): Unit = Global.history.undo()
  @FXML
  def onRedoClicked(e:ActionEvent): Unit = Global.history.redo()

  @FXML
  def onPointerClicked(e:ActionEvent): Unit = {
    setDrawingCursor(Cursor.DEFAULT)
    drawCtrl.removeSelectedShape
  }
  @FXML
  def onCircleClicked(e:ActionEvent): Unit = onDrawShape
  @FXML
  def onRectangleClicked(e:ActionEvent): Unit = onDrawShape
  @FXML
  def onLineClicked(e:ActionEvent): Unit = onDrawShape
  @FXML
  def onPolygonClicked(e:ActionEvent): Unit = onDrawShape

  private def getStrokeColor: Color = strokeColorPicker.getValue
  private def getFillColor: Color = fillColorPicker.getValue
  private def selectedThickness: Int = borderThicknessChooser.getSelectionModel.getSelectedItem
  private def setDrawingCursor(c:Cursor): Unit = drawPanel.setCursor(c)
  private def getWindow = drawPanel.getScene.getWindow
  private def showAnchorsSelected: Boolean = showAnchorsItem.isSelected

  private def selectedShape: Option[SelectedShape] = {
    val btn = Option(btnGroup.getSelectedToggle).map(_.asInstanceOf[ToggleButton])
    btn.map(_.getId).flatMap(shapeBtnsToSelectedShapes.get(_))
  }
}
