package de.thm.move.controllers

import java.net.URL
import java.util.ResourceBundle
import javafx.event.ActionEvent
import javafx.fxml.{Initializable, FXML}
import javafx.scene.canvas.{GraphicsContext, Canvas}
import javafx.scene.control.{ColorPicker, ToggleGroup, Button}
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color

import implicits.FxHandlerImplicits._

class MoveCtrl extends Initializable {
  
  @FXML
  private var mainCanvas: Canvas = _
  @FXML
  private var btnGroup: ToggleGroup = _
  @FXML
  private var  colorPicker: ColorPicker = _

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    colorPicker.setValue(Color.BLACK)

    val drawHandler = { mouseEvent:MouseEvent =>

    }

    mainCanvas.setOnMousePressed(drawHandler)
    mainCanvas.setOnMousePressed(drawHandler)

  }

  @FXML
  def onPointerClicked(e:ActionEvent): Unit = {
    //println( btnGroup.getSelectedToggle )
    println("pointer clicked")
  }


  @FXML
  def onCircleClicked(e:ActionEvent): Unit = {
    drawColored { canvas =>
      canvas.fillOval(10, 60, 30, 30)
    }

    println( btnGroup.getSelectedToggle )
    println("Circle clicked")
  }
  
  @FXML
  def onRectangleClicked(e:ActionEvent): Unit = {
    drawColored { canvas =>
      canvas.fillRect(0, 0, 20, 20)
    }
    println( btnGroup.getSelectedToggle )
    println("rect clicked")
  }

  @FXML
  def onLineClicked(e:ActionEvent): Unit = {
    drawColored { canvas =>
      canvas.strokeLine(5,5, 10,100)
    }

    println("line clicked")
  }

  private def getForegroundColor: Color = colorPicker.getValue

  private def drawColored[A](fn: GraphicsContext => A): A = {
    val context = mainCanvas.getGraphicsContext2D
    val color = getForegroundColor
    context.setFill(color)
    context.setStroke(color)
    fn(context)
  }
}