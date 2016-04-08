/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.views.shapes

import java.net.URI
import javafx.scene.image.{ImageView, Image}

class ResizableImage(val uri:URI, val img:Image) extends ImageView(img) with ResizableShape with RectangleLike {
  setPreserveRatio(true)
  setFitWidth(200)

  override def getWidth: Double = getFitWidth
  override def getHeight: Double = getFitHeight

  override def setWidth(w:Double): Unit = setFitWidth(w)
  override def setHeight(h:Double): Unit = setFitHeight(h)

  override def copy: ResizableShape = {
    val duplicate = new ResizableImage(uri, img)
    duplicate.copyPosition(this)
    duplicate
  }
}
