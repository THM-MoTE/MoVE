package de.thm.move.controllers

import java.net.URL
import java.util.ResourceBundle
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.{Initializable, FXML}
import javafx.scene.canvas.{GraphicsContext, Canvas}
import javafx.scene.control._
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import collection.JavaConversions._

import de.thm.move.models.SelectedShape
import de.thm.move.models.SelectedShape.SelectedShape
import de.thm.move.models.SelectedShape.SelectedShape
import implicits.FxHandlerImplicits._

class MoveCtrl extends Initializable {

  type Point = (Double,Double)

  @FXML
  private var mainCanvas: Canvas = _
  @FXML
  private var btnGroup: ToggleGroup = _
  @FXML
  private var fillColorPicker: ColorPicker = _
  @FXML
  private var strokeColorPicker: ColorPicker = _

  @FXML
  private var borderThicknessChooser: ChoiceBox[Int] = _

  private val shapeBtnsToSelectedShapes = Map(
      "rectangle_btn" -> SelectedShape.Rectangle,
      "circle_btn" -> SelectedShape.Circle,
      "line_btn" -> SelectedShape.Line
    )

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    fillColorPicker.setValue(Color.BLACK)
    strokeColorPicker.setValue(Color.WHITE)

    val sizesList:java.util.List[Int] = (8 until 72 by 2).toList
    borderThicknessChooser.setItems(FXCollections.observableArrayList(sizesList))
    borderThicknessChooser.getSelectionModel.selectFirst()

     var startX = -1.0
     var startY = -1.0

    val drawHandler = { mouseEvent:MouseEvent =>
      if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
        startX = mouseEvent.getX()
        startY = mouseEvent.getY()
      } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
        val endX = mouseEvent.getX()
        val endY = mouseEvent.getY()
        drawCustomShape(startX -> startY, endX -> endY)
      }
    }

    mainCanvas.setOnMousePressed(drawHandler)
    mainCanvas.setOnMouseReleased(drawHandler)

  }

  @FXML
  def onPointerClicked(e:ActionEvent): Unit = {
    //println( btnGroup.getSelectedToggle )
    println("pointer clicked")
  }


  @FXML
  def onCircleClicked(e:ActionEvent): Unit = {
//    drawColored { canvas =>
//      canvas.fillOval(10, 60, 30, 30)
//    }
//
//    println( btnGroup.getSelectedToggle )
    println("Circle clicked")
  }
  
  @FXML
  def onRectangleClicked(e:ActionEvent): Unit = {
//    drawColored { canvas =>
//      canvas.fillRect(0, 0, 20, 20)
//    }
//    println( btnGroup.getSelectedToggle )
    println("rect clicked")
  }

  @FXML
  def onLineClicked(e:ActionEvent): Unit = {
//    drawColored { canvas =>
//      canvas.strokeLine(5,5, 10,100)
//    }

    println("line clicked")
  }

  private def getStrokeColor: Color = strokeColorPicker.getValue
  private def getFillColor: Color = fillColorPicker.getValue
  private def selectedThickness: Int = borderThicknessChooser.getSelectionModel.getSelectedItem

  private def drawColored[A](fn: GraphicsContext => A): A = {
    val context = mainCanvas.getGraphicsContext2D
    context.setFill(getFillColor)
    context.setStroke(getStrokeColor)
    fn(context)
  }

  private def selectedShape: Option[SelectedShape] = {
    val btn = btnGroup.getSelectedToggle.asInstanceOf[ToggleButton]
    Option(btn.getId).flatMap(shapeBtnsToSelectedShapes.get(_))
  }

  private def drawCustomShape(start:Point, end:Point) = {
    drawColored { canvas =>
      val (startX, startY) = start
      val (endX, endY) = end
      selectedShape.foreach {
        case SelectedShape.Rectangle =>
          val width = endX - startX
          val height = endY - startY
          canvas.fillRect(startX, startY, width, height)
        case SelectedShape.Circle =>
          val width = endX - startX
          val height = endY - startY
          canvas.fillOval(startX, startY, width, height)
        case SelectedShape.Line => canvas.strokeLine(startX, startY, endX, endY)
      }

    }
  }
}