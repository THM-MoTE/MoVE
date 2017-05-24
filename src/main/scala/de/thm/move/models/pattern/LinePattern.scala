package de.thm.move.models.pattern

import java.util.function.Predicate
import javafx.scene.Node

import de.thm.move.types.ColorizableNode

sealed trait LinePattern extends ModelicaPattern {
  def cssClass:String
  override def modelicaRepresentation: String = s"LinePattern.${patternName}"

  override def applyToShape(shape:ColorizableNode): Unit = {
    LinePattern.removeOldCss(shape)
    shape.getStyleClass.add(cssClass)
  }
}

case object SNone extends LinePattern {
  override val cssClass: String = "none-stroke"
  override def patternName:String = "None"
  override def toString:String = patternName
}

case object SSolid extends LinePattern {
  override val cssClass: String = "solid-stroke"
  override def patternName:String = "Solid"
  override def toString:String = patternName
}

case object Dash extends LinePattern {
  override val cssClass: String = "dash-stroke"
}
case object Dot extends LinePattern {
  override val cssClass: String = "dotted-stroke"
}
case object DashDot extends LinePattern {
  override val cssClass: String = "dash-dotted-stroke"
}
case object DashDotDot extends LinePattern {
  override val cssClass: String = "dash-dotted-dotted-stroke"
}

object LinePattern {
  val cssRegex = ".*-stroke"

  //remove old stroke style
  val removeOldCss: (Node) => Unit = { shape =>
    shape.getStyleClass().removeIf(new Predicate[String]() {
      override def test(str:String): Boolean = str.`matches`(cssRegex)
    })
  }
  val patternObjects:List[LinePattern] =
    List(SNone, SSolid, Dash, Dot, DashDot, DashDotDot)

  val get:String => LinePattern = ModelicaPattern.getRepresentation(patternObjects)
}
