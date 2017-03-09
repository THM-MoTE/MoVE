package de.thm.move.models.pattern

import de.thm.move.types._

trait ModelicaPattern {
  def patternName:String = toString
  def modelicaRepresentation: String
  def generateModelicaCode: String =
    s"pattern = ${modelicaRepresentation}"

  def applyToShape(shape:ColorizableNode): Unit
}

private[pattern] object ModelicaPattern {
  def getRepresentation[A <:ModelicaPattern](possibleObjects:List[A])(modelicaRepresentation:String):A =
      possibleObjects
        .find(_.modelicaRepresentation == modelicaRepresentation)
        .getOrElse(throw new IllegalArgumentException(s"Couldn't find a Pattern-instance for $modelicaRepresentation"))
}
