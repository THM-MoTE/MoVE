package de.thm.move.controllers

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.canvas.Canvas

class MoveCtrl {
  
  @FXML
  private var mainCanvas: Canvas = _
  
  @FXML
  def onCircleClicked(e:ActionEvent): Unit = {
    mainCanvas.getGraphicsContext2D.fillOval(10, 60, 30, 30);
    println("Circle clicked")
  }
  
  @FXML
  def onRectangleClicked(e:ActionEvent): Unit = {
    println("rect clicked")
  }
}