package de.thm.move.models

import java.nio.file.{Files, Path}

import de.thm.move.loader.parser.ast.Model

import scala.collection.JavaConversions._

case class SrcFile(file:Path, model:Model) {
  private lazy val lines = Files.readAllLines(file).toList

  def getBeforeModel: Option[String] = {
    model.icon map { ico =>
      val startLineIdx = ico.start.line-1
      val beforeLines = lines.take(startLineIdx)
      val beforeChars = lines(startLineIdx).take(ico.start.column-1)
      beforeLines.mkString("\n") + beforeChars
    }
  }

  def getAfterModel: Option[String] = {
    model.icon map { ico =>
      val endLineIdx = ico.end.line-1
      val afterLines = lines.drop(endLineIdx+1)
      val afterChars = lines(endLineIdx).drop(ico.end.column)
      afterChars + "\n" + afterLines.mkString("\n")
    }
  }
}
