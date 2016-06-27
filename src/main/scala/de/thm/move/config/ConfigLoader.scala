/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.config

import java.net.URL

import scala.io.Source

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
