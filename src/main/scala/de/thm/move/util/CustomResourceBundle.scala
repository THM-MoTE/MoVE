package de.thm.move.util

import java.util
import java.util.{Locale, ResourceBundle}
import scala.collection.JavaConverters._

class CustomResourceBundle(files:List[String], locale:Locale) extends ResourceBundle {
  val bundles = for(file <- files) yield ResourceBundle.getBundle(file, locale)

  override def getKeys: util.Enumeration[String] = {
    val keyList = bundles.flatMap { x => x.keySet().asScala }
    val iterator =  keyList.iterator

    new util.Enumeration[String] {
      override def hasMoreElements: Boolean = iterator.hasNext
      override def nextElement(): String = iterator.next
    }
  }

  override def handleGetObject(key: String): AnyRef = {
    bundles.find(_.containsKey(key)).
      map(_.getObject(key)).
      orNull
  }
}
