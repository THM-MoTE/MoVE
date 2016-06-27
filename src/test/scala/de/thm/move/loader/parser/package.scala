/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.loader

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

import org.junit.Assert._
import org.junit.Test

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

  def iconEqual(icon1:Model, icon2:Model): Unit = {
    assertEquals(icon1.name, icon2.name)
    (icon1.annot, icon2.annot) match {
      case (Icon(system1, shapes1, _,_),Icon(system2,shapes2,_,_)) =>
        assertEquals(system1, system2)
        assertEquals(shapes1,shapes2)
      case _ => throw new AssertionError(s"Given icon1 and icon2 aren't both Icons!")
    }
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
