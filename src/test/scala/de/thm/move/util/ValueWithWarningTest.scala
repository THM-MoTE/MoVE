/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.util

import de.thm.move.MoveSpec
import GeometryUtils._

class ValueWithWarningTest extends MoveSpec {

  "The Validation-monad" should "map the containing value" in {
    val v = 5
    val vs = ValidationSuccess(v)
    val mappedVS = vs.map(_.toString)
    mappedVS shouldBe ValidationSuccess("5")
    mappedVS.getValue shouldBe "5"
    mappedVS.getWarnings shouldBe Nil

    val v2 = "Nico"
    val verror = ValidationWarning(v2, List("Error"))
    val mapped = verror.map(_.charAt(0))
    mapped shouldBe ValidationWarning('N', List("Error"))
    mapped.getValue shouldBe 'N'
    mapped.getWarnings shouldBe List("Error")
  }

  it should "combine two Validation-monads using flatMap" in {
    val vs = ValidationSuccess("A")
    val mapped = vs.flatMap { v =>
        ValidationWarning(v.length, List("Length < 2"))
    }
    mapped shouldBe ValidationWarning(1, List("Length < 2"))

    val vs2 = ValidationWarning("A", List("Blup"))
    val mapped2 = vs2.flatMap { v =>
      ValidationSuccess(v.length)
    }
    mapped2 shouldBe ValidationWarning(1, List("Blup"))

    val mapped3 = vs2.flatMap { v =>
        ValidationWarning(v+"N", List("test"))
    }
    mapped3 shouldBe ValidationWarning("AN", List("Blup", "test"))
  }
}
