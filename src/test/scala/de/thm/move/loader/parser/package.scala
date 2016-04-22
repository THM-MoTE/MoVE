package de.thm.move.loader

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

import scala.util._

import de.thm.move.loader.parser.ast._

package object parser {
  private  val parser:ModelicaParserLike = new ModelicaParser
  def parse(str:String): Try[List[Model]] =
    parser.parse(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)))

  val withParseSuccess: String => Model = parse(_) match {
    case Success(elem) => elem.head
    case Failure(exc) => throw exc
  }

  val withException: String => Unit = parse(_) match {
    case Success(_) => throw new IllegalStateException("Expected failure")
    case Failure(_) => //yeay
  }

  def annotationModel(modelname:String, content:String): String =
    s"""
       |model $modelname
       | annotation(
       |  $content
       | );
       |end $modelname;
     """.stripMargin

  def graphicModel(modelname:String, content:String):String = {
    annotationModel(modelname,
    s"""
       |Icon( graphics = {
       |$content
       |})
     """.stripMargin
    )
  }
}
