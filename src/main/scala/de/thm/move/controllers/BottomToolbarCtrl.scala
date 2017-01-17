package de.thm.move.controllers

import java.net.URL
import java.util.ResourceBundle
import javafx.beans.property.{DoubleProperty, SimpleDoubleProperty}
import javafx.event.ActionEvent
import javafx.fxml.{FXML, Initializable}
import javafx.scene.{Node, Parent}
import javafx.scene.control.{Label, ToolBar}
import javafx.scene.layout.Pane

import de.thm.move.implicits.LambdaImplicits._
import de.thm.move.implicits.FxHandlerImplicits
import org.reactfx.EventStreams

class BottomToolbarCtrl extends Initializable {

  @FXML
  var paperSizeLbl: Label = _
  @FXML
  var zoomPercentLbl: Label = _

  private var paneToScale: Pane = _

  val paperWidthProperty:DoubleProperty = new SimpleDoubleProperty(0)
  val paperHeightProperty:DoubleProperty = new SimpleDoubleProperty(0)

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    val widths = EventStreams.valuesOf(paperWidthProperty)
    val heights  = EventStreams.valuesOf(paperHeightProperty)
    EventStreams.combine(widths, heights).map[String](FxHandlerImplicits.function { tuple:org.reactfx.util.Tuple2[Number,Number] =>
      s"${tuple._1.intValue()} x ${tuple._2.intValue()}"
    }).subscribe { x:String => paperSizeLbl.setText(x) }
  }

  def postInitialize(paneToScale:Pane): Unit = {
    this.paneToScale = paneToScale
    zoomPercentLbl.textProperty().bind(
      paneToScale.scaleXProperty().multiply(100).asString("%3.0f%%"))
  }

  @FXML
  def zoomIncreasePressed(e:ActionEvent): Unit = {
    val factor = paneToScale.getScaleX() + 0.1
    paneToScale.setScaleX(factor)
    paneToScale.setScaleY(factor)
  }
  @FXML
  def zoomDecreasePressed(e:ActionEvent): Unit = {
    val factor = paneToScale.getScaleX() - 0.1
    paneToScale.setScaleX(factor)
    paneToScale.setScaleY(factor)
  }
}
