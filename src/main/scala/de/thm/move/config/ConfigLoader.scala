/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.config

import scala.io.Source
import java.net.URL

class ConfigLoader(path:URL) extends Config {
  private val map =
    Source.fromURL(path, "UTF-8").getLines().flatMap { line =>
      if(line.startsWith("#")) List()
      else {
        val splitted = line.split("=")
        if(splitted.length != 2) List()
        else List( (splitted.head.trim, splitted.tail.head.trim) )
      }
    }.toMap

  override def getAll: List[(String,String)] = map.toList
  override def getString(key:String):Option[String] = map.get(key)
}
