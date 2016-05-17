/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.util

import java.net.URI
import java.nio.file.{Files, Path, Paths}
import java.util.Base64
import javafx.scene.paint.Color

import de.thm.move.Global._

object ResourceUtils {

  def getFilename(uri:URI):String = {
    val uriStr = uri.toString
    uriStr.substring(uriStr.lastIndexOf("/")+1, uriStr.length)
  }

  def getFilename(p:Path):String = {
    p.getFileName.toString
  }

  def asColor(key:String): Option[Color] =
    config.getString(key).map(Color.web)

  /** Encodes the given bytes as base64 */
  def encodeBase64(bytes:Array[Byte]): Array[Byte] = {
    val encoder = Base64.getEncoder
    encoder.encode(bytes)
  }

  /** Encodes the given bytes as base64 and returns it as a string.
    * @see [ResourceUtils#encodeBase64]
    */
  def encodeBase64String(bytes:Array[Byte]): String =
    new String(encodeBase64(bytes), encoding)

  /** Copies src into the parent-directory of target */
  def copy(src:URI, target:URI): Unit = {
    val targetPath = Paths.get(target).getParent
    val srcPath = Paths.get(src)
    val filename = srcPath.getFileName
    Files.copy(srcPath, targetPath.resolve(filename))
  }
}
