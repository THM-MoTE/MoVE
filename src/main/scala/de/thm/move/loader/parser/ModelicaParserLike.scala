package de.thm.move.loader.parser

import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Files
import scala.util._

import de.thm.move.loader.parser.ast._

trait ModelicaParserLike {

  def parse(path:Path): Try[Model] = {
    val stream = Files.newInputStream(path)
    val erg = parse(stream)
    stream.close
    erg
  }
  def parse(stream:InputStream): Try[Model]
}

object ModelicaParserLike {
  class ParsingError(msg:String) extends IllegalArgumentException(msg)
}
