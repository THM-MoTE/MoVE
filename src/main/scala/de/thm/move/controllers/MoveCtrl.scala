/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.controllers

import java.net.URL
import java.nio.file.Paths
import java.util.ResourceBundle
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.{FXMLLoader, Initializable, FXML}
import javafx.scene.{Scene, Parent, Cursor}
import javafx.scene.control._
import javafx.scene.input._
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.{Stage, FileChooser}
import de.thm.move.Global
import de.thm.move.util.JFxUtils._
import de.thm.move.views.{SaveDialog, DrawPanel, Anchor}
import de.thm.move.views.shapes.ResizableShape
import de.thm.move.views.Dialogs

import collection.JavaConversions._

import de.thm.move.models.{ModelicaCodeGenerator, SelectedShape}
import de.thm.move.models.ModelicaCodeGenerator.FormatSrc._
import de.thm.move.models.SelectedShape.SelectedShape
import de.thm.move.models.LinePattern._
import de.thm.move.models.LinePattern
import de.thm.move.models.FillPattern._
import de.thm.move.models.FillPattern
import de.thm.move.loader.parser.ModelicaParserLike
import de.thm.move.loader.ShapeConverter
import implicits.FxHandlerImplicits._
import implicits.ConcurrentImplicits._
import implicits.MonadImplicits._

import scala.util._
import java.io.FileInputStream

class MoveCtrl extends Initializable {

  private val aboutStage = new Stage()

  @FXML
  var saveAsMenuItem: MenuItem = _
  @FXML
  var openMenuItem: MenuItem = _

  @FXML
  var undoMenuItem: MenuItem = _
  @FXML
  var redoMenuItem: MenuItem = _

  @FXML
  var deleteMenuItem: MenuItem = _

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
  var linePatternChooser: ChoiceBox[LinePattern] = _
  @FXML
  var fillPatternChooser: ChoiceBox[FillPattern] = _
  @FXML
  var borderThicknessChooser: ChoiceBox[Int] = _

  @FXML
  var drawStub: StackPane = _
  private val drawPanel = new DrawPanel()
  private val drawCtrl = new DrawCtrl(drawPanel, shapeInputHandler)
  private val selectionCtrl = new SelectedShapeCtrl(drawPanel)
  private val aboutCtrl = new AboutCtrl()

  private val moveHandler = drawCtrl.getMoveHandler

  private val shapeBtnsToSelectedShapes = Map(
      "rectangle_btn" -> SelectedShape.Rectangle,
      "circle_btn" -> SelectedShape.Circle,
      "line_btn" -> SelectedShape.Line,
      "path_btn" -> SelectedShape.Path,
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
      Global.shortcuts.getShortcut("move-elements") -> getButtonById("line_pointer"),
      Global.shortcuts.getShortcut("draw-rectangle") -> getButtonById("rectangle_btn"),
      Global.shortcuts.getShortcut("draw-line") -> getButtonById("line_btn"),
      Global.shortcuts.getShortcut("draw-polygon") -> getButtonById("polygon_btn"),
      Global.shortcuts.getShortcut("draw-path") -> getButtonById("path_btn"),
      Global.shortcuts.getShortcut("draw-circle") -> getButtonById("circle_btn")
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

  private def setupAboutDialog(): Unit = {
    //=== setup about dialog
    val aboutWindowWidth = Global.config.getDouble("window.about.width").getOrElse(200.0)
    val aboutWindowHeight = Global.config.getDouble("window.about.height").getOrElse(200.0)
    val fxmlLoader = new FXMLLoader(getClass.getResource("/fxml/about.fxml"))
    fxmlLoader.setController(aboutCtrl)
    val aboutViewRoot: Parent = fxmlLoader.load()
    aboutStage.initOwner(getWindow)
    val scene = new Scene(aboutViewRoot)
    scene.getStylesheets.add(Global.styleSheetUrl)

    aboutStage.setTitle(Global.config.getString("window.title").map(_+" - About").getOrElse(""))
    aboutStage.setScene(scene)
    aboutStage.setWidth(aboutWindowWidth)
    aboutStage.setHeight(aboutWindowHeight)
  }

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    setupShortcuts("save-as", "undo", "redo", "delete-item", "load-image",
      "show-anchors")(saveAsMenuItem, undoMenuItem, redoMenuItem, deleteMenuItem,
        loadImgMenuItem, showAnchorsItem)
    drawStub.getChildren.add(drawPanel)

    val sizesList:java.util.List[Int] = (1 until 20).toList
    borderThicknessChooser.setItems(FXCollections.observableArrayList(sizesList))
    setupDefaultColors()

    val linePatterns = LinePattern.values.toList
    linePatternChooser.setItems(FXCollections.observableList(linePatterns))
    linePatternChooser.setValue(LinePattern.Solid)

    val fillPatterns = FillPattern.values.toList
    fillPatternChooser.setItems(FXCollections.observableList(fillPatterns))
    fillPatternChooser.setValue(FillPattern.Solid)

    val handler = drawCtrl.getDrawHandler

    val drawHandler = { mouseEvent:MouseEvent =>
      selectedShape match {
        case Some(shape) =>
          selectionCtrl.removeSelectedShape
          handler(shape, mouseEvent)(getFillColor, getStrokeColor, selectedThickness)
        case _ => //ignore
      }
    }

    //add eventhandlers
    fillColorPicker.setOnAction(colorPickerChanged _)
    strokeColorPicker.setOnAction(colorPickerChanged _)

    onChoiceboxChanged(borderThicknessChooser)(
      selectionCtrl.setStrokeWidthForSelectedShape)
    onChoiceboxChanged(linePatternChooser)(
      selectionCtrl.setStrokePattern)
    onChoiceboxChanged(fillPatternChooser)(selectionCtrl.setFillPattern)

    drawPanel.setOnMousePressed(drawHandler)
    drawPanel.setOnMouseDragged(drawHandler)
    drawPanel.setOnMouseClicked(drawHandler)
    drawPanel.setOnMouseReleased(drawHandler)
  }

  /** Called after the scene is fully-constructed and displayed.
    * (Used for adding a key-event listener)
    */
  def setupMove(): Unit = {
    setupAboutDialog()

    val combinationsToRunnable = keyCodeToButtons.map {
      case (combination, btn) => combination -> fnRunnable(btn.fire)
    }

    //shortcuts that aren't mapped to buttons
    Global.shortcuts.getKeyCode("draw-constraint").foreach { code =>
      drawStub.getScene.setOnKeyPressed { ke: KeyEvent =>
        if(ke.getCode == code)
          drawCtrl.drawConstraintProperty.set(true)
      }
      drawStub.getScene.setOnKeyReleased { ke: KeyEvent =>
        if(ke.getCode == code)
          drawCtrl.drawConstraintProperty.set(false)
      }
    }

    drawStub.getScene.getAccelerators.putAll(combinationsToRunnable)

    drawStub.requestFocus()
  }

  def colorPickerChanged(ae:ActionEvent): Unit = {
    val src = ae.getSource
    if(src == strokeColorPicker)
      selectionCtrl.setStrokeColorForSelectedShape(strokeColorPicker.getValue)
    else if(src == fillColorPicker)
      selectionCtrl.setFillColorForSelectedShape(fillColorPicker.getValue)
  }

  def shapeInputHandler(ev:InputEvent): Unit = {
    if(selectedShape.isEmpty) {
      ev match {
        case mv:MouseEvent if mv.getEventType == MouseEvent.MOUSE_CLICKED =>
          mv.getSource() match {
            case s:ResizableShape => selectionCtrl.setSelectedShape(s)
            case _:Anchor => //ignore can't change
          }

        case mv: MouseEvent => moveHandler(mv)
        case _ => //not mapped event
      }
    }
  }

  private def showSrcCodeDialog():Option[FormatSrc] = {
    val dialog = new SaveDialog
    val selectOpt:Option[ButtonType] = dialog.showAndWait()
    selectOpt.map {
      case dialog.onelineBtn => Oneline
      case dialog.prettyBtn => Pretty
    }
  }

  val moFileFilter = new FileChooser.ExtensionFilter("Modelica files (*.mo)", "*.mo")

  @FXML
  def onOpenClicked(e:ActionEvent): Unit = {
    val chooser = new FileChooser()

    chooser.setTitle("Save as..")
    chooser.setSelectedExtensionFilter(moFileFilter)
    val fileOp = Option(chooser.showOpenDialog(getWindow))
    for {
      file <- fileOp
      path = Paths.get(file.toURI)
    } {
      val parser = ModelicaParserLike()
      parser.parse(path) match {
        case Success(ast) =>
          val converter = new ShapeConverter(1,
            ShapeConverter.gettCoordinateSystemSizes(ast).head,
            path
            )
          val shapes = converter.getShapes(ast)
          shapes.foreach { s =>
            drawCtrl.addShape(s)
            drawCtrl.addNode(s.getAnchors)
          }
        case Failure(ex) =>
          val excDialog = Dialogs.newExceptionDialog(ex)
          excDialog.showAndWait()
      }

    }
  }

  @FXML
  def onSaveAsClicked(e:ActionEvent): Unit = {
    val chooser = new FileChooser()

    chooser.setTitle("Save as..")
    chooser.setSelectedExtensionFilter(moFileFilter)
    val fileOp = Option(chooser.showSaveDialog(getWindow))

    for (
      file <- fileOp;
      uri = file.toURI;
      srcFormat <- showSrcCodeDialog()
    ) {
      val shapes = drawPanel.getShapes.filterNot(_.isInstanceOf[Anchor])
      val width = drawPanel.getWidth
      val height = drawPanel.getHeight
      val generator = new ModelicaCodeGenerator(srcFormat, width, height)

      val filenamestr = Paths.get(uri).getFileName.toString
      val modelName = if(filenamestr.endsWith(".mo")) filenamestr.dropRight(3) else filenamestr
      val lines = generator.generate(modelName, uri, shapes)
      generator.writeToFile(lines)(uri)
    }
  }

  @FXML
  def onLoadBitmap(e:ActionEvent): Unit = {
    val chooser = new FileChooser()
    chooser.setTitle("Open bitmap")
    val fileOp = Option(chooser.showOpenDialog(getWindow))
    fileOp map { file =>
      file.toURI
    } foreach {
      drawCtrl.drawImage
    }
  }

  private def onDrawShape: Unit = {
    setDrawingCursor(Cursor.CROSSHAIR)
    selectionCtrl.removeSelectedShape
  }

  @FXML
  def onAboutClicked(e:ActionEvent): Unit = aboutStage.show()

  @FXML
  def onShowAnchorsClicked(e:ActionEvent): Unit = drawCtrl.setVisibilityOfAnchors(showAnchorsSelected)
  @FXML
  def onUndoClicked(e:ActionEvent): Unit = Global.history.undo()
  @FXML
  def onRedoClicked(e:ActionEvent): Unit = Global.history.redo()

  @FXML
  def onDeleteClicked(e:ActionEvent): Unit = selectionCtrl.deleteSelectedShape

  @FXML
  def onPointerClicked(e:ActionEvent): Unit = {
    setDrawingCursor(Cursor.DEFAULT)
    selectionCtrl.removeSelectedShape
  }
  @FXML
  def onCircleClicked(e:ActionEvent): Unit = onDrawShape
  @FXML
  def onRectangleClicked(e:ActionEvent): Unit = onDrawShape
  @FXML
  def onLineClicked(e:ActionEvent): Unit = onDrawShape
  @FXML
  def onPathClicked(e:ActionEvent): Unit = onDrawShape
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
