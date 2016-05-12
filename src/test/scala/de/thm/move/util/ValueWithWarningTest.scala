package de.thm.move.util

import org.junit.Assert._
import org.junit.Test
import GeometryUtils._

class ValueWithWarningTest {
  @Test
  def mapTest: Unit = {
    val v = 5
    val vs = ValueSuccess(v)
    val mappedVS = vs.map(_.toString)
    assertEquals(ValueSuccess("5"), mappedVS)
    assertEquals("5", mappedVS.getValue)
    assertEquals(Nil, mappedVS.getWarnings)

    val v2 = "Nico"
    val verror = ValueWarning(v2, List("Error"))
    val mapped = verror.map(_.charAt(0))
    assertEquals(ValueWarning('N', List("Error")), mapped)
    assertEquals('N', mapped.getValue)
    assertEquals(List("Error"), mapped.getWarnings)
  }

  @Test
  def flatMapTest: Unit = {
    val vs = ValueSuccess("A")
    val mapped = vs.flatMap { v =>
        ValueWarning(v.length, List("Length < 2"))
    }
    assertEquals(ValueWarning(1, List("Length < 2")), mapped)

    val vs2 = ValueWarning("A", List("Blup"))
    val mapped2 = vs2.flatMap { v =>
      ValueSuccess(v.length)
    }

    assertEquals(ValueWarning(1, List("Blup")), mapped2)

    val mapped3 = vs2.flatMap { v =>
        ValueWarning(v+"N", List("test"))
    }
    assertEquals(ValueWarning("AN", List("Blup", "test")), mapped3)
  }
}
