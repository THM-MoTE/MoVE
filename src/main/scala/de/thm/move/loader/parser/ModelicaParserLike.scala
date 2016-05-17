/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.loader.parser

import java.io.InputStream
import java.nio.file.{Files, Path}

import de.thm.move.loader.parser.ast._

import scala.util._

/**A parser for modelica files. */
trait ModelicaParserLike {

  /** Parses the file represented by path and returns the parsed ast */
  def parse(path:Path): Try[List[Model]] = {
    val stream = Files.newInputStream(path)
    val erg = parse(stream)
    stream.close()
    erg
  }
  /** Parses the given InputStream and returns the parsed ast */
  def parse(stream:InputStream): Try[List[Model]]
}

object ModelicaParserLike {
  class ParsingError(msg:String) extends IllegalArgumentException(msg)

  def apply(): ModelicaParserLike = new ModelicaParser
}
