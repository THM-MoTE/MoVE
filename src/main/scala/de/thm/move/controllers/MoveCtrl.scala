/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.controllers

import java.net.URL
import java.nio.file.Paths
import java.util.ResourceBundle
import javafx.collections.{ListChangeListener, FXCollections}
import javafx.collections.ListChangeListener.Change
import javafx.event.ActionEvent
import javafx.fxml.{FXMLLoader, Initializable, FXML}
import javafx.scene.control.Alert.AlertType
import javafx.scene.{Scene, Parent, Cursor}
import javafx.scene.control._
import javafx.scene.input._
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.{Stage, FileChooser}
import de.thm.move.Global._
import de.thm.move.config.ValueConfig
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
import de.thm.move.util.PointUtils._
import de.thm.move.util.Convertable._
import implicits.FxHandlerImplicits._
import implicits.ConcurrentImplicits._
import implicits.MonadImplicits._

import scala.util._

/** Main-Controller for all menus,buttons, etc. */
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
  var copyMenuItem: MenuItem = _
  @FXML
  var pasteMenuItem: MenuItem =_
  @FXML
  var duplicateMenuItem: MenuItem = _
  @FXML
  var groupMenuItem: MenuItem = _
  @FXML
  var ungroupMenuItem: MenuItem = _
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
  private val contextMenuCtrl = new ContextMenuCtrl(drawPanel, drawCtrl)
  private val selectionCtrl = new SelectedShapeCtrl(drawPanel)
  private val aboutCtrl = new AboutCtrl()
  private val clipboardCtrl = new ClipboardCtrl[List[ResizableShape]]

  private val fillColorConfig = new ValueConfig(fillColorConfigURI)
  private val strokeColorConfig = new ValueConfig(strokeColorConfigURI)

  private val moveHandler = selectionCtrl.getMoveHandler

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
      shortcuts.getShortcut("move-elements") -> getButtonById("line_pointer"),
      shortcuts.getShortcut("draw-rectangle") -> getButtonById("rectangle_btn"),
      shortcuts.getShortcut("draw-line") -> getButtonById("line_btn"),
      shortcuts.getShortcut("draw-polygon") -> getButtonById("polygon_btn"),
      shortcuts.getShortcut("draw-path") -> getButtonById("path_btn"),
      shortcuts.getShortcut("draw-circle") -> getButtonById("circle_btn"),
      shortcuts.getShortcut("draw-image") -> getButtonById("image_btn")
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
        shortcuts.getShortcut(key) foreach menu.setAccelerator
      }
    }

    /*Setup default colors for fill-,strokeChooser & strokeWidth*/
    private def setupDefaultColors(): Unit = {
      def asColor(key:String): Option[Color] =
        config.getString(key).map(Color.web)

      val fillColor = asColor("colorChooser.fillColor").getOrElse(Color.BLACK)
      val strokeColor = asColor("colorChooser.strokeColor").getOrElse(Color.BLACK)
      val width = config.getInt("colorChooser.strokeWidth").getOrElse(1)

      fillColorPicker.setValue(fillColor)
      strokeColorPicker.setValue(strokeColor)
      borderThicknessChooser.setValue(width)

      //setup custom colors
      fillColorPicker.getCustomColors.addAll(fillColorConfig.getConvertedValues:_*)
      strokeColorPicker.getCustomColors.addAll(strokeColorConfig.getConvertedValues:_*)

      val colorChangedHandler: ValueConfig => ListChangeListener[Color] = conf => new ListChangeListener[Color] {
        override def onChanged(change: Change[_ <: Color]): Unit = {
          while(change.next) {
            if(change.wasAdded)
              change.getAddedSubList.foreach(x => conf.setUniqueValue(x.toString))
            else if(change.wasRemoved)
              change.getRemoved.foreach(x => conf.removeValue(x.toString))
          }
        }
      }

      fillColorPicker.getCustomColors.addListener(colorChangedHandler(fillColorConfig))
      strokeColorPicker.getCustomColors.addListener(colorChangedHandler(strokeColorConfig))
    }

  private def setupAboutDialog(): Unit = {
    //=== setup about dialog
    val aboutWindowWidth = config.getDouble("window.about.width").getOrElse(200.0)
    val aboutWindowHeight = config.getDouble("window.about.height").getOrElse(200.0)
    val fxmlLoader = new FXMLLoader(getClass.getResource("/fxml/about.fxml"))
    fxmlLoader.setController(aboutCtrl)
    val aboutViewRoot: Parent = fxmlLoader.load()
    aboutStage.initOwner(getWindow)
    val scene = new Scene(aboutViewRoot)
    scene.getStylesheets.add(styleSheetUrl)

    aboutStage.setTitle(config.getString("window.title").map(_+" - About").getOrElse(""))
    aboutStage.setScene(scene)
    aboutStage.setWidth(aboutWindowWidth)
    aboutStage.setHeight(aboutWindowHeight)
  }

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    setupShortcuts("open", "save-as", "undo", "redo", "copy", "paste", "duplicate", "delete-item", "load-image",
      "show-anchors")(openMenuItem, saveAsMenuItem, undoMenuItem, redoMenuItem, copyMenuItem,
        pasteMenuItem, duplicateMenuItem, deleteMenuItem, loadImgMenuItem, showAnchorsItem)
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
    val groupHandler = selectionCtrl.getGroupSelectionHandler

    val drawHandler = { mouseEvent:MouseEvent =>
      selectedShape match {
        case Some(shape) =>
          selectionCtrl.removeSelectedShape
          handler(shape, mouseEvent)(getFillColor, getStrokeColor, selectedThickness)
        case _ if mouseEvent.getSource == drawPanel =>
          groupHandler(mouseEvent)
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
    shortcuts.getKeyCode("draw-constraint").foreach { code =>
      drawStub.getScene.addEventHandler(KeyEvent.KEY_PRESSED, { ke:KeyEvent =>
        if(ke.getCode == code)
          drawCtrl.drawConstraintProperty.set(true)
        })
      drawStub.getScene.addEventHandler(KeyEvent.KEY_RELEASED, { ke:KeyEvent =>
        if(ke.getCode == code)
          drawCtrl.drawConstraintProperty.set(false)
      })
    }
    shortcuts.getKeyCode("select-constraint").foreach { code =>
      drawStub.getScene.addEventHandler(KeyEvent.KEY_PRESSED, { ke:KeyEvent =>
        if(ke.getCode == code)
          selectionCtrl.addSelectedShapeProperty.set(true)
      })
      drawStub.getScene.addEventHandler(KeyEvent.KEY_RELEASED, { ke:KeyEvent =>
        if(ke.getCode == code)
          selectionCtrl.addSelectedShapeProperty.set(false)
      })
    }

    drawStub.getScene.getAccelerators.putAll(combinationsToRunnable)

    drawStub.requestFocus()
  }

  def shutdownMove(): Unit = {
    fillColorConfig.saveConfig()
    strokeColorConfig.saveConfig()
  }

  /** Checks that the color has a valid opacity and if not warns the user. */
  private def withCheckedColor(c:Color): Color = {
    val opacity = c.getOpacity()
    val opacityPc = opacity*100
    if(opacity != 1.0 && opacity != 0.0) {
      Dialogs.newWarnDialog(
        f"The given color has a opacity of $opacityPc%2.0f which modelica can't display.\n"+
        "Colors in modelica can have 2 opacitys: either 100% or 0%"
      ).showAndWait()
    }

    c
  }

  def colorPickerChanged(ae:ActionEvent): Unit = {
    val src = ae.getSource
    if(src == strokeColorPicker)
      selectionCtrl.setStrokeColorForSelectedShape(withCheckedColor(strokeColorPicker.getValue))
    else if(src == fillColorPicker)
      selectionCtrl.setFillColorForSelectedShape(withCheckedColor(fillColorPicker.getValue))
  }

  def shapeInputHandler(ev:InputEvent): Unit = {
    if(selectedShape.isEmpty) {
      ev match {
        case mv:MouseEvent if mv.getEventType == MouseEvent.MOUSE_CLICKED =>
          mv.getSource() match {
            case s:ResizableShape =>
              withResizableElement(s) { resizable =>
                selectionCtrl.setSelectedShape(resizable)
              }
            case _:Anchor => //ignore can't change
          }
        case mv: MouseEvent => moveHandler(mv)
        case _ => //not mapped event
      }
      ev.consume //!!! prevent drawPanel from act on this event
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

  private def showScaleDialog(): Option[Int] = {
    val dialog = Dialogs.newScaleDialog()
    val scaleOp:Option[String] = dialog.showAndWait()
    scaleOp.map(_.toInt).filter(x => x>=minScaleFactor && x<=maxScaleFactor)
  }

  val moFileFilter = new FileChooser.ExtensionFilter("Modelica files (*.mo)", "*.mo")

  @FXML
  def onOpenClicked(e:ActionEvent): Unit = {
    val chooser = new FileChooser()

    chooser.setTitle("Save as..")
    chooser.setSelectedExtensionFilter(moFileFilter)
    val fileOp = Option(chooser.showOpenDialog(getWindow))
    (for {
      file <- fileOp
      path = Paths.get(file.toURI)
      scaleFactor <- showScaleDialog()
    } yield {
      val parser = ModelicaParserLike()
      parser.parse(path) match {
        case Success(ast) =>
          val systemSize = ShapeConverter.gettCoordinateSystemSizes(ast).head
          val converter = new ShapeConverter(scaleFactor,
            systemSize,
            path)
          val shapes = converter.getShapes(ast)
          val scaledSystem = systemSize.map(_*scaleFactor)
          shapes.foreach { s =>
            drawPanel.setPrefSize(scaledSystem.x, scaledSystem.y)
            drawPanel.setMinSize(scaledSystem.x, scaledSystem.y)
            drawPanel.setMaxSize(scaledSystem.x, scaledSystem.y)

            drawCtrl.addShape(s)
            drawCtrl.addNode(s.getAnchors)
          }
        case Failure(ex) =>
          val excDialog = Dialogs.newExceptionDialog(ex)
          excDialog.showAndWait()
      }
      Some(())
    }) getOrElse {
      val dialog = Dialogs.newErrorDialog("Can't load the given file or scale the icons." +
      "\nPlease try again with a valid file and scale factor!")
      dialog.showAndWait()
    }
  }

  @FXML
  def onSaveAsClicked(e:ActionEvent): Unit = {
    val chooser = new FileChooser()

    chooser.setTitle("Save as..")
    chooser.setSelectedExtensionFilter(moFileFilter)
    val fileOp = Option(chooser.showSaveDialog(getWindow))

    (for (
      file <- fileOp;
      uri = file.toURI;
      srcFormat <- showSrcCodeDialog();
      pxPerMm <- showScaleDialog()
    ) yield {
      val shapes = drawPanel.getShapes
      val width = drawPanel.getWidth
      val height = drawPanel.getHeight
      val generator = new ModelicaCodeGenerator(srcFormat, pxPerMm, width, height)

      val filenamestr = Paths.get(uri).getFileName.toString
      val modelName = if(filenamestr.endsWith(".mo")) filenamestr.dropRight(3) else filenamestr
      val lines = generator.generate(modelName, uri, shapes)
      generator.writeToFile(lines)(uri)
      Some(())
    }) getOrElse {
      val dialog = Dialogs.newErrorDialog("Can't save to the given path or scale the icons." +
      "\nPlease try again with a valid path and scale factor!")
      dialog.showAndWait()
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

  @FXML
  def onAboutClicked(e:ActionEvent): Unit = aboutStage.show()

  @FXML
  def onShowAnchorsClicked(e:ActionEvent): Unit = drawCtrl.setVisibilityOfAnchors(showAnchorsSelected)
  @FXML
  def onUndoClicked(e:ActionEvent): Unit = history.undo()
  @FXML
  def onRedoClicked(e:ActionEvent): Unit = history.redo()

  @FXML
  def onDeleteClicked(e:ActionEvent): Unit = selectionCtrl.deleteSelectedShape

  @FXML
  def onCopyClicked(e:ActionEvent): Unit = {
    val elements = selectionCtrl.getSelectedElements
    clipboardCtrl.setElement(elements)
  }
  @FXML
  def onPasteClicked(e:ActionEvent): Unit = {
    clipboardCtrl.getElement.map(_.map(_.copy)) foreach(_.foreach(drawCtrl.addShapeWithAnchors))
  }
  @FXML
  def onDuplicateClicked(e:ActionEvent): Unit = {
    val elements = selectionCtrl.getSelectedElements.map(_.copy)
    elements.foreach(contextMenuCtrl.onDuplicateElementPressed(e, _))
  }
  @FXML
  def onGroupPressed(e:ActionEvent): Unit = selectionCtrl.groupSelectedElements()

  @FXML
  def onUngroupPressed(e:ActionEvent): Unit = selectionCtrl.ungroupSelectedElements()

  private def drawToolChanged(c:Cursor): Unit = {
    setDrawingCursor(c)
    selectionCtrl.removeSelectedShape
    drawCtrl.abortDrawingProcess()
  }

  private def onDrawShape: Unit = {
    drawToolChanged(Cursor.CROSSHAIR)
  }

  @FXML
  def onPointerClicked(e:ActionEvent): Unit = {
    drawToolChanged(Cursor.DEFAULT)
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
