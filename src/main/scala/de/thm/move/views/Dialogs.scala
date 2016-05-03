/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.views

import de.thm.move.controllers.implicits.FxHandlerImplicits._

import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import java.io.StringWriter
import java.io.PrintWriter
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.stage.WindowEvent
import javafx.stage.FileChooser

object Dialogs {

  val allFilesFilter  = new FileChooser.ExtensionFilter("All files", "*.*")
  val moFileFilter = new FileChooser.ExtensionFilter("Modelica files (*.mo)", "*.mo")
  val bitmapFileFilter = new FileChooser.ExtensionFilter(
    "Image files (jpg,jpeg,png,gif,bmp)", "*.jpg",
    "*.jpeg","*.png","*.gif", "*.bmp")

  def newExceptionDialog(ex:Throwable, aditionalInfo:String=""): Alert = {
    val alert = new Alert(AlertType.ERROR)
    alert.setTitle("Exception error!");
    alert.setHeaderText("Something terrible happened!");
    alert.setContentText(s"${ex.getMessage} $aditionalInfo");

    // Create expandable Exception.
    val sw = new StringWriter();
    val pw = new PrintWriter(sw);
    ex.printStackTrace(pw);
    val exceptionText = sw.toString();
    val label = new Label("The exception stacktrace was:");

    val textArea = new TextArea(exceptionText);
    textArea.setEditable(false);
    textArea.setWrapText(true);

    textArea.setMaxWidth(Double.MaxValue);
    textArea.setMaxHeight(Double.MaxValue);
    GridPane.setVgrow(textArea, Priority.ALWAYS);
    GridPane.setHgrow(textArea, Priority.ALWAYS);

    val expContent = new GridPane();
    expContent.setMaxWidth(Double.MaxValue);
    expContent.add(label, 0, 0);
    expContent.add(textArea, 0, 1);

    // Set expandable Exception into the dialog pane.
    alert.getDialogPane().setExpandableContent(expContent);
    alert
  }

  def newScaleDialog(): TextInputDialog = {
    val dialog = new TextInputDialog("1")
    dialog.setTitle("Scale factor");
    dialog.setHeaderText("Give a scale factor in px/mm")
    dialog.setContentText("Please enter a valid scale factor between 1 and 100 (default=1):")
    dialog
  }

  def newPaperSizeDialog(width:Double,height:Double): TextInputDialog = {
    val dialog = new TextInputDialog()
    dialog.setTitle("Paper size");
    dialog.setHeaderText("Give a paper size in px")

    val widthLbl = new Label("Width in px:")
    val heightLbl= new Label("Height in px:")

    val widthTxt = new TextField(width.toString)
    val heightTxt = new TextField(height.toString)

    val pane = new GridPane()
    pane.setMaxWidth(Double.MaxValue)
    pane.setHgap(5)
    pane.setVgap(5)
    pane.add(widthLbl, 0,0)
    pane.add(widthTxt, 1,0)
    pane.add(heightLbl, 0,1)
    pane.add(heightTxt, 1,1)
    dialog.getDialogPane.setContent(pane)
    dialog.setOnCloseRequest { _:DialogEvent =>
      val str = widthTxt.getText +";"+ heightTxt.getText
      dialog.setResult(str)
    }
    dialog
  }

  def newGridSizeDialog(cellSize:Int): TextInputDialog = {
    val dialog = new TextInputDialog()
    dialog.setTitle("Grid size");
    dialog.setHeaderText("Give a grid size in px")
    val sizeLbl = new Label ("Size in px:")
    val sizeTxt = new TextField(cellSize.toString)
    val pane = new GridPane()
    pane.setMaxWidth(Double.MaxValue)
    pane.setHgap(5)
    pane.setVgap(5)
    pane.add(sizeLbl, 0,0)
    pane.add(sizeTxt, 1,0)
    dialog.getDialogPane.setContent(pane)
    dialog.setOnCloseRequest { _:DialogEvent =>
      dialog.setResult(sizeTxt.getText)
    }
    dialog
  }

  def newErrorDialog(msg:String): Alert = {
    val dialog = new Alert(AlertType.ERROR)
    dialog.setTitle("An error occured")
    dialog.setHeaderText("Ouh something didn't work!")
    dialog.setContentText(msg)
    dialog
  }

  def newWarnDialog(msg:String): Alert = {
    val dialog = new Alert(AlertType.WARNING)
    dialog.setTitle("A warning occured")
    dialog.setHeaderText(msg)
    dialog
  }

  private def newFileChooser(
    selectedFilter:FileChooser.ExtensionFilter)(
    fileFilters:FileChooser.ExtensionFilter*): FileChooser = {
      val chooser = new FileChooser()
      chooser.getExtensionFilters().addAll(fileFilters:_*)
      chooser.setSelectedExtensionFilter(selectedFilter)
      chooser
    }

  def newModelicaFileChooser(): FileChooser =
    newFileChooser(moFileFilter)(allFilesFilter, moFileFilter)

  def newBitmapFileChooser(): FileChooser =
    newFileChooser(bitmapFileFilter)(allFilesFilter, bitmapFileFilter)
}
