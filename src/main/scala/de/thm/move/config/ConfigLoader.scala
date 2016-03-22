package de.thm.move.config

import scala.io.Source
import java.net.URL

class ConfigLoader(path:String) extends Config {
  private val map =
    Source.fromURL(new URL(path), "UTF_8").getLines().flatMap { line =>
      val splitted = line.split("=")
      if(splitted.length != 2) List()
      else List( (splitted.head, splitted.tail.head) )
    }.toMap

  override def getString(key:String):Option[String] = map.get(key)
}
