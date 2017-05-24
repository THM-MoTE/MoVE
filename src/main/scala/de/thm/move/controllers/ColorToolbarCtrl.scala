/**
  * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
  *
  * This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
  */

package de.thm.move.controllers

import scala.collection.JavaConverters._
import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, Initializable}
import javafx.scene.control._
import javafx.collections.ListChangeListener.Change
import javafx.collections.{FXCollections, ListChangeListener}
import javafx.event.ActionEvent
import javafx.scene.paint.Color

import de.thm.move.util.ResourceUtils
import de.thm.move.config.ValueConfig
import de.thm.move.Global
import de.thm.move.models.pattern._
import de.thm.move.util.JFxUtils._
import de.thm.move.views.dialogs.Dialogs


/** Controller for the color toolbar (toolbar below the menu).
  */
class ColorToolbarCtrl extends Initializable {

	@FXML
	var strokeColorLabel: Label = _
	@FXML
	var fillColorPicker: ColorPicker = _
	@FXML
	var fillColorLabel: Label = _
	@FXML
	var strokeColorPicker: ColorPicker = _
	@FXML
	var linePatternChooser: ChoiceBox[LinePattern] = _
	@FXML
	var fillPatternChooser: ChoiceBox[FillPattern] = _
	@FXML
	var borderThicknessChooser: ChoiceBox[Int] = _

  private val fillColorConfig = new ValueConfig(Global.fillColorConfigURI)
  private val strokeColorConfig = new ValueConfig(Global.strokeColorConfigURI)

  private var selectionCtrl:SelectedShapeCtrl = _

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
		setupDefaultColors()
    setupPattern()
    val sizesList:java.util.List[Int] = (1 until 20).asJava
    borderThicknessChooser.setItems(FXCollections.observableArrayList(sizesList))
  }

  def postInitialize(selectionCtrl:SelectedShapeCtrl): Unit = {
    this.selectionCtrl = selectionCtrl

    onChoiceboxChanged(borderThicknessChooser)(
      this.selectionCtrl.setStrokeWidth)
    onChoiceboxChanged(linePatternChooser)(
      this.selectionCtrl.setStrokePattern)
    onChoiceboxChanged(fillPatternChooser)(this.selectionCtrl.setFillPattern)
  }

  def shutdown(): Unit = {
    fillColorConfig.saveConfig()
    strokeColorConfig.saveConfig()
  }

  @FXML def colorPickerChanged(ae:ActionEvent): Unit = {
    val src = ae.getSource
    if(src == strokeColorPicker)
      selectionCtrl.setStrokeColor(withCheckedColor(strokeColorPicker.getValue))
    else if(src == fillColorPicker)
      selectionCtrl.setFillColor(withCheckedColor(fillColorPicker.getValue))
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

  private def setupPattern(): Unit = {
    val linePatterns = LinePattern.patternObjects
    linePatternChooser.setItems(FXCollections.observableList(linePatterns.asJava))
    linePatternChooser.setValue(SSolid)

    val fillPatterns = FillPattern.patternObjects
    fillPatternChooser.setItems(FXCollections.observableList(fillPatterns.asJava))
    fillPatternChooser.setValue(FSolid)
  }

  /*Setup default colors for fill-,strokeChooser & strokeWidth*/
  private def setupDefaultColors(): Unit = {
    val fillColor = ResourceUtils.asColor("colorChooser.fillColor").getOrElse(Color.BLACK)
    val strokeColor = ResourceUtils.asColor("colorChooser.strokeColor").getOrElse(Color.BLACK)
    val width = Global.config.getInt("colorChooser.strokeWidth").getOrElse(1)

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
            change.getAddedSubList.asScala.foreach(x => conf.setUniqueValue(x.toString))
          else if(change.wasRemoved)
            change.getRemoved.asScala.foreach(x => conf.removeValue(x.toString))
        }
      }
    }

    fillColorPicker.getCustomColors.addListener(colorChangedHandler(fillColorConfig))
    strokeColorPicker.getCustomColors.addListener(colorChangedHandler(strokeColorConfig))
  }


  def getStrokeColor: Color = strokeColorPicker.getValue
  def getFillColor: Color = fillColorPicker.getValue
  def selectedThickness: Int = borderThicknessChooser.getSelectionModel.getSelectedItem
}
