/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.controllers

import java.net.URL
import java.nio.file.Paths
import java.nio.file.Path
import java.util.ResourceBundle
import javafx.collections.ListChangeListener.Change
import javafx.collections.{FXCollections, ListChangeListener}
import javafx.event.ActionEvent
import javafx.fxml.{FXML, FXMLLoader, Initializable}
import javafx.scene.control._
import javafx.scene.input._
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.{Cursor, Parent, Scene}
import javafx.stage.Stage

import de.thm.move.Global._
import de.thm.move.config.ValueConfig
import de.thm.move.controllers.implicits.ConcurrentImplicits._
import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.controllers.implicits.MonadImplicits._
import de.thm.move.loader.ShapeConverter
import de.thm.move.loader.parser.ModelicaParserLike
import de.thm.move.loader.parser.ast.Model
import de.thm.move.models.FillPattern
import de.thm.move.models.FillPattern._
import de.thm.move.models.LinePattern
import de.thm.move.models.LinePattern._
import de.thm.move.models.ModelicaCodeGenerator.FormatSrc._
import de.thm.move.models.SelectedShape.SelectedShape
import de.thm.move.models._
import de.thm.move.util.Convertable._
import de.thm.move.util.JFxUtils._
import de.thm.move.util.PointUtils._
import de.thm.move.util.ResourceUtils
import de.thm.move.views._
import de.thm.move.views.shapes.ResizableShape

import scala.None
import scala.collection.JavaConversions._
import scala.util._

/** Main-Controller for all menus,buttons, etc. */
class MoveCtrl extends Initializable {

  private val aboutStage = new Stage()
  private var rootStage:Stage = _

  @FXML
  var saveMenuItem: MenuItem = _
  @FXML
  var saveAsMenuItem: MenuItem = _
  @FXML
  var openMenuItem: MenuItem = _
  @FXML
  var chPaperSizeMenuItem: MenuItem = _
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
  var showGridItem: CheckMenuItem = _
  @FXML
  var enableGridItem: CheckMenuItem = _
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
  private val snapGrid = new SnapGrid(drawPanel,
    config.getInt("grid-cell-size").getOrElse(20),
    config.getInt("grid-snap-distance").getOrElse(5)
    )
  private val drawCtrl = new DrawCtrl(drawPanel, shapeInputHandler)
  private val contextMenuCtrl = new ContextMenuCtrl(drawPanel, drawCtrl)
  private val selectionCtrl = new SelectedShapeCtrl(drawCtrl,  snapGrid)
  private val aboutCtrl = new AboutCtrl()
  private val fileCtrl = new FileCtrl(getWindow)
  private val clipboardCtrl = new ClipboardCtrl[List[ResizableShape]]

  private var openedFile:SrcFile = _

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
      shortcuts.getShortcut("draw-circle") -> getButtonById("circle_btn")
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
    setupShortcuts("open", "save", "save-as", "undo", "redo", "copy", "paste", "duplicate", "delete-item","group-elements", "ungroup-elements","load-image",
      "show-anchors", "show-grid", "enable-snapping")(openMenuItem, saveMenuItem, saveAsMenuItem, undoMenuItem, redoMenuItem, copyMenuItem,
        pasteMenuItem, duplicateMenuItem, deleteMenuItem, groupMenuItem, ungroupMenuItem, loadImgMenuItem, showAnchorsItem, showGridItem, enableGridItem)

    //only show the grid if it's enabled
    val visibleFlag = config.getBoolean("grid-visibility").getOrElse(true)
    val snapping  = config.getBoolean("snapping-mode").getOrElse(true)
    val snappingFlag = if(!visibleFlag) false else snapping
    snapGrid.gridVisibleProperty.set(visibleFlag)
    snapGrid.snappingProperty.set(snappingFlag)
    //adjust menu-items to loaded value
    showGridItem.setSelected(visibleFlag)
    enableGridItem.setSelected(snappingFlag)

    drawStub.getChildren.addAll(snapGrid, drawPanel)

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
  def setupMove(stage:Stage): Unit = {
    setupAboutDialog()
    rootStage = stage

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


  @FXML
  def onOpenClicked(e:ActionEvent): Unit = {
    fileCtrl.openFile match {
      case Success((system, shapes)) =>
        drawPanel.setSize(system)
        shapes foreach drawCtrl.addShapeWithAnchors
      case Failure(ex:UserInputException) =>
        Dialogs.newErrorDialog(ex.msg).showAndWait()
      case Failure(ex) =>
        Dialogs.newExceptionDialog(ex).showAndWait()
    }
  }

  private def displayUsedFile(p:Path): Unit = {
    val oldTitle = rootStage.getTitle
    val newTitle = if(oldTitle.contains("-")) {
      val titleStub = oldTitle.take(oldTitle.lastIndexOf("-"))
      titleStub.trim + " - " + ResourceUtils.getFilename(p)
    } else {
      oldTitle.trim + " - " + ResourceUtils.getFilename(p)
    }

    rootStage.setTitle(newTitle)
  }

  @FXML
  def onSaveClicked(e:ActionEvent): Unit = {
    fileCtrl.saveFile(drawPanel.getShapes, drawPanel.getWidth, drawPanel.getHeight).
      map(displayUsedFile) match {
        case Failure(ex:UserInputException) =>
          Dialogs.newErrorDialog(ex.msg).showAndWait()
        case Failure(ex) =>
          Dialogs.newExceptionDialog(ex).showAndWait()
          case _ => //ignore successfull case
      }
  }

  @FXML
  def onSaveAsClicked(e:ActionEvent): Unit = {
    fileCtrl.saveNewFile(drawPanel.getShapes, drawPanel.getWidth, drawPanel.getHeight).
      map(displayUsedFile) match {
        case Failure(ex:UserInputException) =>
          Dialogs.newErrorDialog(ex.msg).showAndWait()
        case Failure(ex) =>
          Dialogs.newExceptionDialog(ex).showAndWait()
          case _ => //ignore successfull case
      }
  }

  @FXML
  def onChPaperSizeClicked(e:ActionEvent): Unit = {
    val strOpt:Option[String] = Dialogs.newPaperSizeDialog(drawPanel.getWidth, drawPanel.getHeight).showAndWait()
    strOpt.flatMap { x =>
      try {
        val ar = x.split(";")
        val width = ar(0).toDouble
        val height = ar(1).toDouble
        Some((width,height))
      } catch {
        case _:NumberFormatException => None
        case _:IndexOutOfBoundsException => None
      }
    } filter {
      case (x,y) => x>0 && y>0
    } match {
      case Some((width, height)) => drawPanel.setSize(width, height)
      case None =>
        Dialogs.newErrorDialog("Given Papersize can't be used!\n" +
        "Please specify 2 valid numbers >0")
    }
  }

  @FXML
  def onLoadBitmap(e:ActionEvent): Unit = {
    fileCtrl.openImage foreach {
      drawCtrl.drawImage
    }
  }

  @FXML
  def onAboutClicked(e:ActionEvent): Unit = aboutStage.show()

  @FXML
  def onShowAnchorsClicked(e:ActionEvent): Unit = drawCtrl.setVisibilityOfAnchors(showAnchorsSelected)

  @FXML
  def onShowGridClicked(e:ActionEvent): Unit = {
    val flag = showGridItem.isSelected
    snapGrid.gridVisibleProperty.set(flag)
    if(!flag) { //not visible; disable snapping
      enableGridItem.fire()
    }
  }
  @FXML
  def onEnableGridClicked(e:ActionEvent): Unit = {
    val flag = enableGridItem.isSelected
    if(!showGridItem.isSelected) {
      //visible is false => disable snapping-mode
      enableGridItem.setSelected(false)
      snapGrid.snappingProperty.set(false)
    } else {
      snapGrid.snappingProperty.set(flag)
    }
  }

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
