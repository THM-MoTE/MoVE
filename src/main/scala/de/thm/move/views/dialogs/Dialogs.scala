/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.views.dialogs

import java.io.{PrintWriter, StringWriter}
import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.layout.{GridPane, Priority}
import javafx.stage.FileChooser

import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.util.StringMarshaller

object Dialogs {

  val allFilesFilter  = new FileChooser.ExtensionFilter("All files", "*.*")
  val moFileFilter = new FileChooser.ExtensionFilter("Modelica files (*.mo)", "*.mo")
  val svgFileFilter = new FileChooser.ExtensionFilter("Svg files (*.svg)", "*.svg")
  val pngFileFilter = new FileChooser.ExtensionFilter("Png files (*.png)", "*.png")
  val bitmapFileFilter = new FileChooser.ExtensionFilter(
    "Image files (jpg,jpeg,png,gif,bmp)", "*.jpg",
    "*.jpeg","*.png","*.gif", "*.bmp")

  def newExceptionDialog(ex:Throwable, aditionalInfo:String=""): Alert = {
    val alert = new Alert(AlertType.ERROR)
    alert.setTitle("Exception error!")
    alert.setHeaderText("Something terrible happened!")
    alert.setContentText(s"${ex.getMessage} $aditionalInfo")

    // Create expandable Exception.
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)
    ex.printStackTrace(pw)
    val exceptionText = sw.toString()
    val label = new Label("The exception stacktrace was:")

    val textArea = new TextArea(exceptionText)
    textArea.setEditable(false)
    textArea.setWrapText(true)

    textArea.setMaxWidth(Double.MaxValue)
    textArea.setMaxHeight(Double.MaxValue)
    GridPane.setVgrow(textArea, Priority.ALWAYS)
    GridPane.setHgrow(textArea, Priority.ALWAYS)

    val expContent = new GridPane()
    expContent.setMaxWidth(Double.MaxValue)
    expContent.add(label, 0, 0)
    expContent.add(textArea, 0, 1)

    // Set expandable Exception into the dialog pane.
    alert.getDialogPane().setExpandableContent(expContent)
    alert
  }

  def newScaleDialog()(implicit marshaller:StringMarshaller[Int]): InputDialog[Int] = {
    val dialog = new InputDialog("scale factor" -> Some(1))
    dialog.setTitle("Scale factor")
    dialog.setHeaderText("Give a scale factor in px/mm")
    dialog.setContentText("Please enter a valid scale factor between 1 and 100 (default=1):")
    dialog
  }

  def newPaperSizeDialog(width:Double,height:Double): TextInputDialog = {
    val dialog = new TextInputDialog()
    dialog.setTitle("Paper size")
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
    dialog.setTitle("Grid size")
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

  def newSvgFileChooser(): FileChooser = newFileChooser(svgFileFilter)(allFilesFilter, svgFileFilter)
  def newPngFileChooser(): FileChooser = newFileChooser(pngFileFilter)(allFilesFilter, pngFileFilter)

  def newConfirmationDialog(additionalInfo:String = ""):Alert = {
    val alert = new Alert(AlertType.CONFIRMATION)
    alert.setTitle("Confirm this action")
    alert.setHeaderText(s"Are you sure?\n$additionalInfo")
    alert
  }

  def newListDialog[A](xs:List[A], aditionalInfo:String=""): Alert = {
    val alert = new Alert(AlertType.WARNING)
    alert.setTitle("Warnings!")
    alert.setHeaderText(aditionalInfo)

    // Create expandable Exception.
    val label = new Label("List of warnings:")
    val text = xs.mkString("\n")
    val textArea = new TextArea(text)
    textArea.setEditable(false)
    textArea.setWrapText(true)

    textArea.setMaxWidth(Double.MaxValue)
    textArea.setMaxHeight(Double.MaxValue)
    GridPane.setVgrow(textArea, Priority.ALWAYS)
    GridPane.setHgrow(textArea, Priority.ALWAYS)

    val listContent = new GridPane()
    listContent.setMaxWidth(Double.MaxValue)
    listContent.add(label, 0, 0)
    listContent.add(textArea, 0, 1)

    // Set expandable Exception into the dialog pane.
    alert.getDialogPane.setExpandableContent(listContent)
    alert.getDialogPane.setExpanded(true)
    alert
  }
}
