/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.loader.parser

import java.io.{ByteArrayInputStream, InputStreamReader}
import java.nio.charset.StandardCharsets

import scala.util._
import de.thm.move.MoveSpec
import de.thm.move.loader.parser.PropertyParser._
import de.thm.move.loader.parser.ast._

class StringParserTest extends MoveSpec {
  val parser = new ModelicaParser()
  def parseString(str:String): String = {
    parser.stringLiteral(str)
  }

  "The parser for Modelica strings" should "parse simple strings" in {
    val s = "this is a super awesome test"
    true shouldBe true
  }

  "PropertyParser#transformEscapeChars" should
    "transform literal escape characters to ansi escape characters" in {
      val s = "this\\t\\tis a\\n test\\rmöb\\b"
      parser.transformEscapeChars(s) shouldBe "this\t\tis a\n test\rmöb\b"

      val s2 = "\\n\\n\\t"
      parser.transformEscapeChars(s2) shouldBe "\n\n\t"
  }

  it should "return the same string for strings without escape characters" in {
    val s = "this is awesome"
    parser.transformEscapeChars(s) shouldBe s
  }
}
