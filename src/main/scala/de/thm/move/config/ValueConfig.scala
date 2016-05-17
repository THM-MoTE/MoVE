/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.config

import java.net.URL
import java.nio.charset.Charset
import java.nio.file.{Files, Paths}

import de.thm.move.util.Convertable

import scala.collection._
import scala.io.Source

class ValueConfig(url:URL) {
  val values = Source.fromURL(url, "UTF-8").getLines().to[mutable.ArrayBuffer]

  def getValues:List[String] = values.filter(!_.startsWith("##")).toList
  def getConvertedValues[A](implicit ev:Convertable[String, A]):List[A] = getValues.map(ev.convert)
  def setUniqueValue(v:String) = if(!values.contains(v)) setValue(v)
  def setValue(v:String) = values += v

  def removeValue(v:String):Unit = values.remove(values.indexOf(v))

  def saveConfig(): Unit = {
    val writer = Files.newBufferedWriter(Paths.get(url.toURI), Charset.forName("UTF-8"))
    writer.write(values.mkString("\n"))
    writer.close()
  }
}
