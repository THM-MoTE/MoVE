/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.views.shapes

import java.net.URI
import javafx.scene.image.{ImageView, Image}

class ResizableImage(val srcEither:Either[URI, Array[Byte]], val img:Image)
  extends ImageView(img)
  with ResizableShape
  with RectangleLike {
  setPreserveRatio(true)

  /* When preserving-ratio resize only 1 side; the other gets adjusted */
  val resizeWidth = img.getWidth > img.getHeight
  if(resizeWidth) setFitWidth(200)
  else setFitHeight(200)

  override def getWidth: Double = {
    if(resizeWidth) getFitWidth //get the fitting width
    else getBoundsInLocal.getWidth //get the calculated with
  }
  override def getHeight: Double = {
    if(!resizeWidth) getFitHeight //get the fitting height
    else getBoundsInLocal.getHeight //get the calculated height
  }

  override def setWidth(w:Double): Unit = {
    if(resizeWidth) setFitWidth(w) //set fitting width
    else () //don't do anything; this side gets calculated according to the height
  }
  override def setHeight(h:Double): Unit = {
    if(!resizeWidth) setFitHeight(h)
    else ()
  }

  override def copy: ResizableShape = {
    val duplicate = new ResizableImage(srcEither, img)
    duplicate.copyPosition(this)
    duplicate
  }
}

object ResizableImage {
  def apply(uri:URI, img:Image) = new ResizableImage(Left(uri), img)
  def apply(bytes:Array[Byte], img:Image) = new ResizableImage(Right(bytes), img)
}
