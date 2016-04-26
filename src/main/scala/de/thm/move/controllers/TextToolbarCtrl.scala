/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.controllers

import java.util.ResourceBundle
import java.net.URL
import javafx.event.ActionEvent
import javafx.scene.control._
import javafx.scene.text.Font
import javafx.scene.paint.Color
import javafx.collections.FXCollections
import javafx.fxml.{FXML, FXMLLoader, Initializable}
import implicits.FxHandlerImplicits._

import de.thm.move.util.ResourceUtils
import de.thm.move.util.JFxUtils._

import scala.collection.JavaConverters._

/** Controller for the TopTextToolbar.
  *
  * Please be aware that a correct SelectedShapeCtrl is set after creating this controller and before it's
  * first use! Invoke in parent's Controller#initizalize() method the TextToolbarCtrl#setSelectedShapeCtrl()!
  */
class TextToolbarCtrl extends Initializable {

  private var selectedShapeCtrl: SelectedShapeCtrl = _

  @FXML var fontFamilyChooser:ChoiceBox[String] = _
  @FXML var fontSizeChooser:ChoiceBox[Int] = _

  @FXML var fontColorLbl:Label = _
  @FXML var fontColorChooser:ColorPicker = _

  @FXML var boldBtn:ToggleButton = _
  @FXML var italicBtn:ToggleButton = _
  @FXML var underlineBtn:ToggleButton = _

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    fontColorChooser.setOnAction { _:ActionEvent =>
      fontColorLbl.setTextFill(fontColorChooser.getValue)
    }
    val fontColor = ResourceUtils.asColor("colorChooser.strokeColor").getOrElse(Color.BLACK)
    fontColorChooser.setValue(fontColor)

    val fontNames = Font.getFamilies().asScala.distinct.asJava
    fontFamilyChooser.setItems(FXCollections.observableArrayList(fontNames))
    fontFamilyChooser.setValue(Font.font("Arial").getFamily)

    val sizesList = (8 until 72 by 2).asJava
    fontSizeChooser.setItems(FXCollections.observableArrayList(sizesList))
    fontSizeChooser.setValue(12)
  }

  /** Sets the controller for selected elements.
   * Due to the fact, that the FXMLLoader can't inject object-attributes we need a way to add fields
   * after creating the controller!
   */
  def setSelectedShapeCtrl(ctrl:SelectedShapeCtrl): Unit = {
    selectedShapeCtrl = ctrl
    onChoiceboxChanged(fontFamilyChooser)((selectedShapeCtrl.setFont _) compose Font.font )
    onChoiceboxChanged(fontSizeChooser)(selectedShapeCtrl.setFontSize)
    fontColorChooser.setOnAction { _:ActionEvent =>
      selectedShapeCtrl.setFontColor(fontColorChooser.getValue)
    }

    //font style buttons
    boldBtn.setOnAction { _:ActionEvent =>
      selectedShapeCtrl.setFontBold(boldBtn.isSelected)
    }
    italicBtn.setOnAction { _:ActionEvent =>
      selectedShapeCtrl.setFontItalic(italicBtn.isSelected)
    }
    underlineBtn.setOnAction { _:ActionEvent =>
      selectedShapeCtrl.setFontUnderline(underlineBtn.isSelected)
    }
  }

  def getFontColor: Color = fontColorChooser.getValue
  def getFontSize: Int = fontSizeChooser.getValue
  def getFont: Font = Font.font(fontFamilyChooser.getValue, getFontSize)
}
