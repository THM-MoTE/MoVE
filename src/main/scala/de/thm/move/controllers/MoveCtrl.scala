 /**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.controllers

import java.io.InputStream
import java.net.URL
import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import java.util.ResourceBundle
import javafx.application.Platform
import javafx.collections.ListChangeListener.Change
import javafx.collections.{FXCollections, ListChangeListener}
import javafx.event.ActionEvent
import javafx.fxml.{FXML, Initializable}
import javafx.scene.control._
import javafx.scene.input._
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.{Cursor, Group, Parent, Scene}
import javafx.stage.Stage

import de.thm.move.types._
import de.thm.move.Global._
import de.thm.move.config.ValueConfig
import de.thm.move.implicits.ConcurrentImplicits._
import de.thm.move.implicits.FxHandlerImplicits
import de.thm.move.implicits.FxHandlerImplicits._
import de.thm.move.implicits.MonadImplicits._
import de.thm.move.implicits.LambdaImplicits._
import de.thm.move.models.FillPattern._
import de.thm.move.models.LinePattern._
import de.thm.move.models.SelectedShape.SelectedShape
import de.thm.move.models._
import de.thm.move.util.converters.Convertable._
import de.thm.move.util.JFxUtils._
import de.thm.move.util.ResourceUtils
import de.thm.move.views.anchors.Anchor
import de.thm.move.views.dialogs.Dialogs
import de.thm.move.views.panes.{DrawPanel, SnapGrid}
import de.thm.move.views.shapes.{ResizableShape, ResizableText}
import de.thm.recent.{MRecent, Recent}
import de.thm.recent.JsProtocol._

import scala.None
import scala.collection.JavaConversions._
import scala.util._
import org.reactfx.EventStreams
import org.reactfx.EventStream

/** Main-Controller for all menus,buttons, etc. */
class MoveCtrl extends Initializable {

  private var rootStage:Stage = _

  //====================== menus
  @FXML
  var newMenuItem: MenuItem = _
  @FXML
  var saveMenuItem: MenuItem = _
  @FXML
  var saveAsMenuItem: MenuItem = _
  @FXML
  var openMenuItem: MenuItem = _
  @FXML
  var recentFilesMenu: Menu =_
  @FXML
  var chPaperSizeMenuItem: MenuItem = _
  @FXML
  var closeMenuItem: MenuItem = _
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
  //====================== top toolbar's
  @FXML
  var topToolbarStack: StackPane = _
  @FXML
  var embeddedColorToolbar: ToolBar = _
  @FXML
  var embeddedTextMenu: Parent = _
  @FXML
  var embeddedBottomToolbar: ToolBar = _
  //====================== bottom toolbar
  @FXML
  var embeddedTextMenuController: TextToolbarCtrl = _
  @FXML
  var embeddedColorToolbarController: ColorToolbarCtrl = _
  @FXML
  var embeddedBottomToolbarController: BottomToolbarCtrl = _

  @FXML
  var drawStub: StackPane = _
  private val drawPanel = new DrawPanel()
  private var snapGrid = new SnapGrid(drawPanel,
    config.getInt("grid-cell-size").getOrElse(20),
    config.getInt("grid-snap-distance").getOrElse(5)
    )
  private val drawPanelCtrl = new DrawPanelCtrl(drawPanel, shapeInputHandler)
  private val drawCtrl = new DrawCtrl(drawPanelCtrl)
  private val contextMenuCtrl = new ContextMenuCtrl(drawPanel, drawPanelCtrl)
  private val selectionCtrl = new SelectedShapeCtrl(drawPanelCtrl,  snapGrid)
  private val (aboutStage, _) = AboutCtrl.setupAboutDialog()
  private lazy val fileCtrl = new FileCtrl(getWindow)
  private val clipboardCtrl = new ClipboardCtrl[List[ResizableShape]]

  private val moveHandler = selectionCtrl.getMoveHandler

  private val shapeBtnsToSelectedShapes = Map(
      "rectangle_btn" -> SelectedShape.Rectangle,
      "circle_btn" -> SelectedShape.Circle,
      "line_btn" -> SelectedShape.Line,
      "path_btn" -> SelectedShape.Path,
      "polygon_btn" -> SelectedShape.Polygon,
      "text_btn" -> SelectedShape.Text
    )

  /**
   * Maps registered KeyCodes (from shortcuts.conf) to corresponding button
    * '''! Ensure that this field is initialized AFTER all fields are initiazlized !'''
    */
  private lazy val keyCodeToButtons = {
    val buttons =
      embeddedBottomToolbar.getItems.collect { case x:ButtonBase => x } ++
      btnGroup.getToggles.map(_.asInstanceOf[ButtonBase])
    def getButtonById(id:String): Option[ButtonBase] = {
      buttons.find(_.getId == id)
    }

    val keyCodeOpts = List(
      shortcuts.getShortcut("move-elements") -> getButtonById("line_pointer"),
      shortcuts.getShortcut("draw-rectangle") -> getButtonById("rectangle_btn"),
      shortcuts.getShortcut("draw-line") -> getButtonById("line_btn"),
      shortcuts.getShortcut("draw-polygon") -> getButtonById("polygon_btn"),
      shortcuts.getShortcut("draw-path") -> getButtonById("path_btn"),
      shortcuts.getShortcut("draw-circle") -> getButtonById("circle_btn"),
      shortcuts.getShortcut("draw-text") -> getButtonById("text_btn"),
      shortcuts.getShortcut("zoom-plus") -> getButtonById("zoomBtnIncrease"),
      shortcuts.getShortcut("zoom-minus") -> getButtonById("zoomBtnDecrease"))

    val codes = keyCodeOpts flatMap {
      case (Some(code),Some(btn)) => List( (code, btn) )
      case _ => Nil
    }
    codes.toMap
  }


    /*Setup given keyboard shortcuts to given item*/
    private def setupShortcuts(keyMenus: (String,MenuItem)*) = {
      for (
        (key, menu) <- keyMenus
      ) {
        shortcuts.getShortcut(key) foreach menu.setAccelerator
      }
    }

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    setupShortcuts(
      "new" -> newMenuItem,
      "open" -> openMenuItem,
      "save" -> saveMenuItem,
      "save-as" -> saveAsMenuItem,
      "ch-paper-size" -> chPaperSizeMenuItem,
      "close" -> closeMenuItem,
      "undo" -> undoMenuItem,
      "redo" -> redoMenuItem,
      "copy" -> copyMenuItem,
      "paste" -> pasteMenuItem,
      "duplicate" -> duplicateMenuItem,
      "delete-item" -> deleteMenuItem,
      "group-elements" -> groupMenuItem,
      "ungroup-elements" -> ungroupMenuItem,
      "load-image" -> loadImgMenuItem,
      "show-anchors" -> showAnchorsItem,
      "show-grid" -> showGridItem,
      "enable-snapping" -> enableGridItem)


    //TODO implement action
    def recentClickedHandler(path:Path): Unit = println(s"user clicked on $path")

    val recent =
      if(Files.exists(recentFilesPath)) MRecent(Recent.fromInputStream[Path](Files.newInputStream(recentFilesPath)))
      else MRecent(Recent.fromList(Seq[Path]()))

    val recentHandler = new RecentlyFilesHandler(recent, recentClickedHandler)
    recentFilesMenu.getItems.addAll(recentHandler.getMenuItems:_*)

    embeddedTextMenuController.setSelectedShapeCtrl(selectionCtrl)
    embeddedColorToolbarController.postInitialize(selectionCtrl)
    embeddedBottomToolbarController.postInitialize(drawStub)
    embeddedBottomToolbarController.paperWidthProperty.bind(drawPanel.prefWidthProperty())
    embeddedBottomToolbarController.paperHeightProperty.bind(drawPanel.prefHeightProperty())

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

    drawPanel.setSize(config.getDouble("drawpane-width").getOrElse(400),
      config.getDouble("drawpane-height").getOrElse(400))


    val handler = drawCtrl.getDrawHandler
    val groupHandler = selectionCtrl.getGroupSelectionHandler

    val drawHandler = { mouseEvent:MouseEvent =>
      selectedShape match {
        case Some(SelectedShape.Text) =>
          selectionCtrl.unselectShapes()
          if(mouseEvent.getEventType == MouseEvent.MOUSE_CLICKED) {
            drawCtrl.drawText(mouseEvent.getX,mouseEvent.getY,
              embeddedTextMenuController.getFontColor,
              embeddedTextMenuController.getFont)
          }
        case Some(shape) =>
          selectionCtrl.unselectShapes()
          handler(shape, mouseEvent)(embeddedColorToolbarController.getFillColor,
                                     embeddedColorToolbarController.getStrokeColor,
                                     embeddedColorToolbarController.selectedThickness)
        case _ if mouseEvent.getSource == drawPanel =>
          groupHandler(mouseEvent)
        case _ => //ignore
      }
    }

    drawPanel.setOnMousePressed(drawHandler)
    drawPanel.setOnMouseDragged(drawHandler)
    drawPanel.setOnMouseClicked(drawHandler)
    drawPanel.setOnMouseReleased(drawHandler)
  }

  /** Called after the scene is fully-constructed and displayed.
    * (Used for adding a key-event listener)
    */
  def setupMove(stage:Stage, fileParameter:Option[String]): Unit = {
    //call it after reflection calls to make sure the window isn't null
    aboutStage.initOwner(getWindow)
    rootStage = stage

    val combinationsToRunnable = keyCodeToButtons.map {
      case (combination, btn) => combination -> fnRunnable(btn.fire)
    }

    val pressedStream = EventStreams.eventsOf(drawStub.getScene, KeyEvent.KEY_PRESSED)
    val releasedStream = EventStreams.eventsOf(drawStub.getScene, KeyEvent.KEY_RELEASED)

      //shortcuts that aren't mapped to buttons
    shortcuts.getKeyCode("draw-constraint").foreach { code =>
      pressedStream.
        filter(byKeyCode(code)).
        subscribe { drawCtrl.drawConstraintProperty.set(true) }
      releasedStream.
        filter(byKeyCode(code)).
        subscribe { drawCtrl.drawConstraintProperty.set(false) }
    }

    shortcuts.getKeyCode("select-constraint").foreach { code =>
      pressedStream.
        filter(byKeyCode(code)).
        subscribe { selectionCtrl.addSelectedShapeProperty.set(true) }
      releasedStream.
        filter(byKeyCode(code)).
        subscribe { selectionCtrl.addSelectedShapeProperty.set(false) }
    }

    setupMoveShapesByShortcuts(drawStub.getScene)
    drawStub.getScene.getAccelerators.putAll(combinationsToRunnable)

    drawStub.requestFocus()

    fileParameter.map(Paths.get(_)).foreach(openFile)
  }

  private def setupMoveShapesByShortcuts(scene:Scene) = {
    val (deltaX,deltaY) = config.
      getPoint("shortcut-moving-delta-x", "shortcut-moving-delta-y").
      getOrElse((5.0,5.0))

    val releasedStream = EventStreams.eventsOf(scene, KeyEvent.KEY_RELEASED)
    shortcuts.getKeyCode("move-left") foreach { code =>
      releasedStream.
        filter(byKeyCode(code)).
        subscribe {
          val directioned = (deltaX*(-1), 0.0)
          selectionCtrl.move(directioned)
        }
    }
    shortcuts.getKeyCode("move-right") foreach { code =>
      releasedStream.
        filter(byKeyCode(code)).
        subscribe {
          val directioned = (deltaX, 0.0)
          selectionCtrl.move(directioned)
        }
    }
    shortcuts.getKeyCode("move-up") foreach { code =>
      releasedStream.
        filter(byKeyCode(code)).
        subscribe {
          val directioned = (0.0, deltaY*(-1))
          selectionCtrl.move(directioned)
        }
    }
    shortcuts.getKeyCode("move-down") foreach { code =>
      releasedStream.
        filter(byKeyCode(code)).
        subscribe {
          val directioned = (0.0, deltaY)
          selectionCtrl.move(directioned)
        }
    }
  }

  def shutdownMove(): Unit = {
    embeddedColorToolbarController.shutdown()
  }

  def shapeInputHandler(ev:InputEvent): Unit = {
    if(selectedShape.isEmpty) {
      ev match {
        case mv:MouseEvent if mv.getEventType == MouseEvent.MOUSE_CLICKED &&
                              mv.getButton == MouseButton.PRIMARY &&
                              mv.getClickCount() == 2 =>
          selectionCtrl.rotationMode()
        case mv:MouseEvent if mv.getEventType == MouseEvent.MOUSE_CLICKED =>
          //user selects an element
          mv.getSource() match {
            case s:ResizableShape =>
              if(s.isInstanceOf[ResizableText]) embeddedTextMenu.toFront()
              else embeddedColorToolbar.toFront()

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
  def onNewClicked(e:ActionEvent): Unit = {
    val selectOpt:Option[ButtonType] = Dialogs.newConfirmationDialog("Unsaved changes will be lost!").showAndWait()
    selectOpt.foreach {
      case ButtonType.OK => drawPanelCtrl.removeAll()
      case _ => //do nothing; abort
    }
  }

  @FXML
  def onOpenClicked(e:ActionEvent): Unit = {
    setupOpenedFile(fileCtrl.openFile)
  }

  def openFile(file:Path): Unit = {
    val fileInfos = fileCtrl.openFile(file).map {
      case (point, shapes) => (file, point, shapes)
    }
    setupOpenedFile(fileInfos)
  }

  private def setupOpenedFile(fileInfos: Try[(Path, Point,List[ResizableShape])]): Unit = {
    fileInfos match {
      case Success((file, system, shapes)) =>
        displayUsedFile(file)
        drawPanel.setSize(system)
        if (drawPanelCtrl.getElements.nonEmpty) {
          drawPanelCtrl.removeAll()
        }
        shapes foreach drawPanelCtrl.addShapeWithAnchors
      case Failure(ex:UserInputException) =>
        Dialogs.newErrorDialog(ex.msg).showAndWait()
      case Failure(ex) =>
        Dialogs.newExceptionDialog(ex).showAndWait()
    }
  }

  /** Displays the file behind p in the title of move's main window */
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

  /** Handles errors from tr by displaying them in a popup-dialog. */
  private def fileErrorHandling(tr: Try[_]): Unit = {
    tr  match {
      case Failure(ex:UserInputException) =>
        Dialogs.newErrorDialog(ex.msg).showAndWait()
      case Failure(ex) =>
        Dialogs.newExceptionDialog(ex).showAndWait()
      case _ => //ignore successfull case
    }
  }

  @FXML
  def onSaveClicked(e:ActionEvent): Unit = {
    fileErrorHandling(
      fileCtrl.saveFile(drawPanel.getShapes, drawPanel.getWidth, drawPanel.getHeight).
      map(displayUsedFile)
    )
  }

  @FXML
  def onSaveAsClicked(e:ActionEvent): Unit = {
    fileErrorHandling(
      fileCtrl.saveAsFile(drawPanel.getShapes, drawPanel.getWidth, drawPanel.getHeight).
        map(displayUsedFile)
    )
  }

  @FXML
  def onExportSvgClicked(e:ActionEvent): Unit = {
    fileErrorHandling(
      fileCtrl.exportAsSvg(drawPanel.getShapes, drawPanel.getWidth,drawPanel.getHeight)
    )
  }

  @FXML
  def onExportBitmapClicked(e:ActionEvent): Unit = {
    fileErrorHandling {
        //temporary pane for making a snapshot,
        //this pane doesn't hold anchors or selection-rectangles
      val shapePanel = new DrawPanel()
      val shapes = drawPanel.getShapes
      //create a copy of all shapes and add them to the new temporary pane
      shapes flatMap {
        case rs:ResizableShape => List(rs.copy)
        case _ => Nil
      } foreach shapePanel.drawShape
      fileCtrl.exportAsBitmap(shapePanel)
    }
  }

  @FXML
  def onChPaperSizeClicked(e:ActionEvent): Unit = {
    val strOpt:Option[List[Double]] = Dialogs.newPaperSizeDialog(drawPanel.getWidth, drawPanel.getHeight).showAndWait()
    strOpt.flatMap { xs =>
      try {
        Some(xs.head, xs(1))
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
  def onChGridSizeClicked(e:ActionEvent): Unit = {
    val strOpt:Option[List[Int]] = Dialogs.newGridSizeDialog(snapGrid.cellSize).showAndWait()
    strOpt.flatMap { x =>
      try {
        Some(x.head)
      } catch {
        case _:NumberFormatException => None
      }
    } filter { x => x>0 } match {
      case Some(size) =>
        drawStub.getChildren.remove(snapGrid)
        snapGrid = snapGrid.setCellSize(size)
        drawStub.getChildren.add(0, snapGrid)
      case None =>
      Dialogs.newErrorDialog("Given Gridsize can't be used!\n" +
      "Please specify a valid number > 0")
    }
  }

  @FXML
  def onClosePressed(e:ActionEvent): Unit = Platform.exit()

  @FXML
  def onLoadBitmap(e:ActionEvent): Unit = {
    fileCtrl.openImage foreach {
      drawCtrl.drawImage
    }
  }

  @FXML
  def onAboutClicked(e:ActionEvent): Unit = aboutStage.show()

  @FXML
  def onShowAnchorsClicked(e:ActionEvent): Unit = drawPanelCtrl.setVisibilityOfAnchors(showAnchorsSelected)

  @FXML
  def onShowGridClicked(e:ActionEvent): Unit = {
    val flag = showGridItem.isSelected
    snapGrid.gridVisibleProperty.set(flag)
    if(!flag) { //not visible; disable snapping
      enableGridItem.fire()
    } else enableGridItem.setDisable(false)
  }
  @FXML
  def onEnableGridClicked(e:ActionEvent): Unit = {
    val flag = enableGridItem.isSelected
    if(!showGridItem.isSelected) {
      //visible is false => disable snapping-mode
      enableGridItem.setSelected(false)
      enableGridItem.setDisable(true)
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
  def onDeleteClicked(e:ActionEvent): Unit = selectionCtrl.deleteSelectedShape()

  @FXML
  def onCopyClicked(e:ActionEvent): Unit = {
    val elements = selectionCtrl.getSelectedShapes
    clipboardCtrl.setElement(elements)
  }
  @FXML
  def onPasteClicked(e:ActionEvent): Unit = {
    clipboardCtrl.getElement.map(_.map(_.copy)).foreach(_.foreach { shape =>
      val p = (for {
        x <- config.getDouble("shift-copied-element-x")
        y <- config.getDouble("shift-copied-element-y")
      } yield (x,y)) getOrElse((0.0,0.0))
      shape.move(p) //shift element a little bit to left &  top
      drawPanelCtrl.addShapeWithAnchors(shape)
    })
  }
  @FXML
  def onDuplicateClicked(e:ActionEvent): Unit = {
    val elements = selectionCtrl.getSelectedShapes.map(_.copy)
    elements.foreach(contextMenuCtrl.onDuplicateElementPressed(_)(e))
  }
  @FXML
  def onGroupPressed(e:ActionEvent): Unit = selectionCtrl.groupSelectedElements()
  @FXML
  def onUngroupPressed(e:ActionEvent): Unit = selectionCtrl.ungroupSelectedElements()

  private def drawToolChanged(c:Cursor): Unit = {
    setDrawingCursor(c)
    selectionCtrl.unselectShapes()
    drawCtrl.abortDrawingProcess()
  }

  private def onDrawShape: Unit = {
    embeddedColorToolbar.toFront()
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
  @FXML
  def onTextClicked(e:ActionEvent): Unit = {
    embeddedTextMenu.toFront()
    drawToolChanged(Cursor.TEXT)
  }

  private def setDrawingCursor(c:Cursor): Unit = drawPanel.setCursor(c)
  private def getWindow = drawPanel.getScene.getWindow
  private def showAnchorsSelected: Boolean = showAnchorsItem.isSelected

  private def selectedShape: Option[SelectedShape] = {
    val btn = Option(btnGroup.getSelectedToggle).map(_.asInstanceOf[ToggleButton])
    btn.map(_.getId).flatMap(shapeBtnsToSelectedShapes.get(_))
  }
}
