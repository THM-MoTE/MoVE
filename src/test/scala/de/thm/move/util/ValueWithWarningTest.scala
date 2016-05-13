package de.thm.move.util

import org.junit.Assert._
import org.junit.Test
import GeometryUtils._

class ValueWithWarningTest {
  @Test
  def mapTest: Unit = {
    val v = 5
    val vs = ValidationSuccess(v)
    val mappedVS = vs.map(_.toString)
    assertEquals(ValidationSuccess("5"), mappedVS)
    assertEquals("5", mappedVS.getValue)
    assertEquals(Nil, mappedVS.getWarnings)

    val v2 = "Nico"
    val verror = ValidationWarning(v2, List("Error"))
    val mapped = verror.map(_.charAt(0))
    assertEquals(ValidationWarning('N', List("Error")), mapped)
    assertEquals('N', mapped.getValue)
    assertEquals(List("Error"), mapped.getWarnings)
  }

  @Test
  def flatMapTest: Unit = {
    val vs = ValidationSuccess("A")
    val mapped = vs.flatMap { v =>
        ValidationWarning(v.length, List("Length < 2"))
    }
    assertEquals(ValidationWarning(1, List("Length < 2")), mapped)

    val vs2 = ValidationWarning("A", List("Blup"))
    val mapped2 = vs2.flatMap { v =>
      ValidationSuccess(v.length)
    }

    assertEquals(ValidationWarning(1, List("Blup")), mapped2)

    val mapped3 = vs2.flatMap { v =>
        ValidationWarning(v+"N", List("test"))
    }
    assertEquals(ValidationWarning("AN", List("Blup", "test")), mapped3)
  }
}
