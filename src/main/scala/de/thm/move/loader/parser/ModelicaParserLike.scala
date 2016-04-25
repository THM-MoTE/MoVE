/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.loader.parser

import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Files
import scala.util._

import de.thm.move.loader.parser.ast._

trait ModelicaParserLike {

  def parse(path:Path): Try[List[Model]] = {
    val stream = Files.newInputStream(path)
    val erg = parse(stream)
    stream.close
    erg
  }
  def parse(stream:InputStream): Try[List[Model]]
}

object ModelicaParserLike {
  class ParsingError(msg:String) extends IllegalArgumentException(msg)

  def apply(): ModelicaParserLike = new ModelicaParser
}
