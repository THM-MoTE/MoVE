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

import de.thm.move.Global._
import de.thm.move.implicits.FxHandlerImplicits._
import de.thm.move.util.converters.StringMarshaller

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
    alert.setTitle(fontBundle.getString("alert.exc.title"))
    alert.setHeaderText(fontBundle.getString("alert.exc.header"))
    alert.setContentText(s"${ex.getMessage} $aditionalInfo")

    // Create expandable Exception.
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)
    ex.printStackTrace(pw)
    val exceptionText = sw.toString()
    val label = new Label(fontBundle.getString("alert.exc.stacktrace"))

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
    val dialog = new InputDialog(fontBundle.getString("scaledialog.inputfield") -> Some(1))
    dialog.setTitle(fontBundle.getString("scaledialog.title"))
    dialog.setHeaderText(fontBundle.getString("scaledialog.header"))
    dialog.setContentText(fontBundle.getString("scaledialog.content"))
    dialog
  }

  def newPaperSizeDialog(width:Double,height:Double)(implicit marshaller:StringMarshaller[Double]): InputDialog[Double] = {
    val dialog = new InputDialog(fontBundle.getString("inputfield-width") -> Some(width), fontBundle.getString("papersizedialog.inputfield-height") -> Some(height))
    dialog.setTitle(fontBundle.getString("papersizedialog.title"))
    dialog.setHeaderText(fontBundle.getString("papersizedialog.header"))
    dialog
  }

  def newGridSizeDialog(cellSize:Int)(implicit marshaller:StringMarshaller[Int]): InputDialog[Int] = {
    val dialog = new InputDialog(fontBundle.getString("gridsizedialog.inputfield") -> Some(cellSize))
    dialog.setTitle(fontBundle.getString("gridsizedialog.title"))
    dialog.setHeaderText(fontBundle.getString("gridsizedialog.header"))
    dialog
  }

  def newErrorDialog(msg:String): Alert = {
    val dialog = new Alert(AlertType.ERROR)
    dialog.setTitle(fontBundle.getString("alert.error.title"))
    dialog.setHeaderText(fontBundle.getString("alert.error.header"))
    dialog.setContentText(msg)
    dialog
  }

  def newWarnDialog(msg:String): Alert = {
    val dialog = new Alert(AlertType.WARNING)
    dialog.setTitle(fontBundle.getString("alert.warning.title"))
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
    alert.setTitle(fontBundle.getString("alert.confirmation.title"))
    alert.setHeaderText(fontBundle.getString("alert.confirmation.header")+s"\n$additionalInfo")
    alert
  }

  def newListDialog[A](xs:List[A], aditionalInfo:String=""): Alert = {
    val alert = new Alert(AlertType.WARNING)
    alert.setTitle(fontBundle.getString("alert.warning.title"))
    alert.setHeaderText(aditionalInfo)

    // Create expandable Exception.
    val label = new Label(fontBundle.getString("alert.warning.list"))
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
