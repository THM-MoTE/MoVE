/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.controllers

import java.net.URL
import java.util.ResourceBundle
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.{FXML, Initializable}
import javafx.scene.control._
import javafx.scene.paint.Color
import javafx.scene.text.{Font, TextAlignment}

import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.util.JFxUtils._
import de.thm.move.util.ResourceUtils

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
  @FXML var alignmentGroup:ToggleGroup = _

  private def alignmentById(id:String): TextAlignment = id match {
    case "align_left_btn" => TextAlignment.LEFT
    case "align_center_btn" => TextAlignment.CENTER
    case "align_right_btn" => TextAlignment.RIGHT
    case _ => throw new IllegalArgumentException(s"No alignment for $id")
  }

  override def initialize(location: URL, resources: ResourceBundle): Unit = {

    val fontColor = ResourceUtils.asColor("colorChooser.strokeColor").getOrElse(Color.BLACK)
    fontColorChooser.setValue(fontColor)

    val fontNames = Font.getFamilies().asScala.distinct.asJava
    fontFamilyChooser.setItems(FXCollections.observableArrayList(fontNames))
    fontFamilyChooser.setValue(Font.font("Arial").getFamily)

    val sizesList = (8 until 72 by 2).asJava
    fontSizeChooser.setItems(FXCollections.observableArrayList(sizesList))
    fontSizeChooser.setValue(12)

    val alignmentToggles = alignmentGroup.getToggles().asScala.map(_.asInstanceOf[ToggleButton])
    alignmentToggles foreach { x =>
      x.setOnAction { ae:ActionEvent =>
        ae.getSource match {
          case x:ToggleButton => selectedShapeCtrl.setTextAlignment(alignmentById(x.getId))
          case _ => //ignore
        }
      }
    }
  }

  /** Sets the controller for selected elements.
   * Due to the fact, that the FXMLLoader can't inject object-attributes we need a way to add fields
   * after creating the controller!
   */
  def setSelectedShapeCtrl(ctrl:SelectedShapeCtrl): Unit = {
    selectedShapeCtrl = ctrl
    onChoiceboxChanged(fontFamilyChooser)(selectedShapeCtrl.setFontName)
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
