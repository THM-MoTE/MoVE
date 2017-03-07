package de.thm.move.models.pattern

import de.thm.move.types._

trait ModelicaPattern {
  def patternName:String = toString
  def modelicaRepresentation: String
  def generateModelicaCode: String =
    s"pattern = ${modelicaRepresentation}"

  def applyToShape(shape:ColorizableNode): Unit
}
