package de.thm.move.views.shapes

import javafx.beans.value.ObservableValue
import javafx.scene.input.MouseEvent
import javafx.scene.shape.Rectangle

import de.thm.move.models.CommonTypes.Point
import de.thm.move.views.Anchor
import de.thm.move.controllers.implicits.FxHandlerImplicits._

class ResizableRectangle(
            startPoint:Point,
            width:Double,
            height:Double) extends Rectangle(startPoint._1, startPoint._2, width, height) with ResizableShape with ColorizableShape {
  private val (x,y) = startPoint

  //create resize anchors
  private val topLeftAnchor = new Anchor(x,y)
  private val topRightAnchor = new Anchor(x+width,y)
  private val bottomLeftAnchor = new Anchor(x,y+height)
  private val bottomRightAnchor = new Anchor(x+width, y+height)

  topLeftAnchor.centerXProperty().bind(xProperty())
  topLeftAnchor.centerYProperty().bind(yProperty())

  topRightAnchor.centerXProperty().bind(xProperty().add(widthProperty()))
  topRightAnchor.centerYProperty().bind(yProperty())

  bottomLeftAnchor.centerXProperty().bind(xProperty())
  bottomLeftAnchor.centerYProperty().bind(yProperty().add(heightProperty()))

  bottomRightAnchor.centerXProperty().bind(xProperty().add(widthProperty()))
  bottomRightAnchor.centerYProperty().bind(yProperty().add(heightProperty()))

  topLeftAnchor.setOnMouseDragged({me:MouseEvent =>
    val (newX, newY) = (me.getX, me.getY)
    val (oldX, oldY) = (this.getX, this.getY)
    val deltaY = if(newY < oldY) getHeight + (oldY-newY)else getHeight - (newY-oldY)
    val deltaX = if(newX > oldX) getWidth + (oldX-newX) else getWidth - (newX-oldX)
    this.setY(newY)
    this.setHeight(deltaY)
    this.setX(newX)
    this.setWidth(deltaX)
  })

  topRightAnchor.setOnMouseDragged({ me: MouseEvent =>
    val (newX, newY) = (me.getX, me.getY)
    val (oldX, oldY) = (this.getX+this.getWidth, this.getY)
    val deltaY = if(newY < oldY) getHeight + (oldY-newY) else getHeight - (newY-oldY)
    val deltaX = if(newX > oldX) getWidth + (newX-oldX) else getWidth - (oldX-newX)

    this.setY(newY)
    this.setHeight(deltaY)
    this.setWidth(deltaX)
  })

  bottomLeftAnchor.setOnMouseDragged({ me: MouseEvent =>
    val (newX, newY) = (me.getX, me.getY)
    val (oldX, oldY) = (this.getX, this.getY+this.getHeight)
    val deltaY = if(newY > oldY) getHeight + (newY-oldY) else getHeight - (oldY-newY)
    val deltaX = if(newX < oldX) getWidth + (oldX-newX) else getWidth - (newX-oldX)

    this.setY(newY)
    //this.setX(newX)
    this.setHeight(deltaY)
    //this.setWidth(deltaX)
  })

  bottomRightAnchor.setOnMouseDragged({ me: MouseEvent =>
    val (newX, newY) = (me.getX, me.getY)
    val (oldX, oldY) = (this.getX+this.getWidth, this.getY+this.getHeight)
    val deltaX = (newX-oldX) + getWidth
    val deltaY = (newY-oldY) + getHeight
    setWidth(deltaX)
    setHeight(deltaY)
  })

//  topLeftAnchor.centerYProperty().addListener({ (ov: ObservableValue[_ <: Number], oldN: Number, newN: Number) =>
//    val oldY = oldN.doubleValue()
//    val newY = newN.doubleValue()
//    val oldHeight = this.getHeight
//    val delta = if(newY > oldY) oldHeight - (newY-oldY) else oldHeight + (oldY-newY)
//    this.setY(newY)
//    this.setHeight(delta)
//  })
//
//  topLeftAnchor.centerXProperty().addListener({ (ov: ObservableValue[_ <: Number], oldN: Number, newN: Number) =>
//    val oldX = oldN.doubleValue()
//    val newX = newN.doubleValue()
//    val oldWidth = this.getWidth
//    this.setX(newX)
//    this.setWidth(oldX + oldWidth - newX)
//  })
//
//  topRightAnchor.centerXProperty().addListener({ (ov: ObservableValue[_ <: Number], oldN: Number, newN: Number) =>
//    val oldX = oldN.doubleValue()
//    val newX = newN.doubleValue()
//    val oldWidth = this.getWidth
//    val delta = newX-oldX
//    this.setWidth(oldWidth + delta)
//  })
//
//  topRightAnchor.centerYProperty().addListener({ (ov: ObservableValue[_ <: Number], oldN: Number, newN: Number) =>
//    val oldY = oldN.doubleValue()
//    val newY = newN.doubleValue()
//    val oldHeight = this.getHeight
//    val delta = oldHeight + (newY - oldY)*(-1)
//    this.setY(newY)
//    this.setHeight(delta)
//  })
//
//  bottomLeftAnchor.centerXProperty().addListener({ (ov: ObservableValue[_ <: Number], oldN: Number, newN: Number) =>
//    val oldX = oldN.doubleValue()
//    val newX = newN.doubleValue()
//    val oldWidth = this.getWidth
//    val delta = oldX - newX
//    this.setX(newX)
//    this.setWidth(oldWidth + delta)
//  })
//
//  bottomLeftAnchor.centerYProperty().addListener({ (ov: ObservableValue[_ <: Number], oldN: Number, newN: Number) =>
//    val oldY = oldN.doubleValue()
//    val newY = newN.doubleValue()
//    val oldHeight = this.getHeight
//    val delta = newY - oldY
//    this.setHeight(oldHeight + delta)
//  })
//
//  bottomRightAnchor.centerXProperty().addListener({ (ov: ObservableValue[_ <: Number], oldN: Number, newN: Number) =>
//      val oldX = oldN.doubleValue()
//      val newX = newN.doubleValue()
//      val oldWidth = this.getWidth
//      val delta = if(newX > oldX) oldWidth + (newX - oldX) else oldWidth - (oldX - newX)
//      this.setWidth(delta)
//    })
//  bottomRightAnchor.centerYProperty().addListener({ (ov: ObservableValue[_ <: Number], oldN: Number, newN: Number) =>
//    val oldY = oldN.doubleValue()
//    val newY = newN.doubleValue()
//    val oldHeight = this.getHeight
//    val delta = if(newY > oldY) oldHeight + (newY - oldY) else oldHeight - (oldY - newY)
//    this.setHeight(delta)
//  })

  val getAnchors: List[Anchor] = List(topLeftAnchor, topRightAnchor, bottomLeftAnchor, bottomRightAnchor)

  //bindAnchorsTranslationToShapesLayout(this)(getAnchors:_*)
}
