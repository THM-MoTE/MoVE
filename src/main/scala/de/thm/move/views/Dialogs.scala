/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.views

import javafx.scene.control.Alert.AlertType
import javafx.scene.control.TextInputDialog
import javafx.scene.control.Alert
import java.io.StringWriter
import java.io.PrintWriter
import javafx.scene.layout.GridPane
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.Priority

object Dialogs {
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
}
