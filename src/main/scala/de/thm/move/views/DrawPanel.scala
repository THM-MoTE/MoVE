package de.thm.move.views

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.value.{ObservableValue}
import javafx.event.{EventHandler}
import javafx.geometry.Bounds
import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape._
import javafx.scene.image.{ImageView, Image}
import javafx.scene.input.{InputEvent}
import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.models.CommonTypes._
import javafx.beans.binding.Bindings

import de.thm.move.views.shapes._

class DrawPanel(inputEventHandler:InputEvent => Unit) extends Pane {
  private var shapes = List[Node]()

  def getShapes:List[Node] = shapes

  def setShapes(xs:List[_ <: Node]) = shapes = xs

  private def addInputEventHandler(node:Node) = {
    node.addEventHandler(InputEvent.ANY, new EventHandler[InputEvent]() {
      override def handle(event: InputEvent): Unit = inputEventHandler(event)
    })
  }

  def drawShape[T <: Node](n:T):Unit = {
    addInputEventHandler(n)
    super.getChildren.add(n)

    shapes = n :: shapes
  }

  def drawShapes[T <: Node](shapes:T*) = shapes foreach drawShape
}
