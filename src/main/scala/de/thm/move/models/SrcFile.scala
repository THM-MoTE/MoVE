/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.models

import java.nio.file.{Files, Path}

import de.thm.move.loader.parser.ast._

import scala.collection.JavaConversions._

/** Represents a source-file with corresponding parsed AST. */
case class SrcFile(file:Path, model:Model) {
  private lazy val lines = Files.readAllLines(file).toList

  /** Gets the source before Icon(...) */
  def getBeforeModel: String = {
    model.annot match {
      case Icon(_,_,start,_) =>
        val startLineIdx = start.line-1
        val beforeLines = lines.take(startLineIdx)
        val beforeChars = lines(startLineIdx).take(start.column-1)
        beforeLines.mkString("\n") + beforeChars
      case NoAnnotation(pos) =>
        val startLineIdx = pos.line-1
        val beforeLines = lines.take(startLineIdx)
        val beforeChars = lines(startLineIdx).take(pos.column-1)
        //add "annotation(" to the source-code before Icon generation
        beforeLines.mkString("\n") + beforeChars + "\n" + "annotation("
      case WithoutIcon(pos) =>
        val startLineIdx = pos.line-1
        val beforeLines = lines.take(startLineIdx)
        val beforeChars = lines(startLineIdx).take(pos.column-1)
        beforeLines.mkString("\n") + beforeChars
    }
  }

  /** Gets the source after Icon(..) */
  def getAfterModel: String = {
    model.annot match {
      case Icon(_,_,start,end) =>
        val endLineIdx = end.line-1
        val afterLines = lines.drop(endLineIdx+1)
        val afterChars = lines(endLineIdx).drop(end.column)
        afterChars + "\n" + afterLines.mkString("\n")
      case WithoutIcon(pos) =>
        val endLineIdx = pos.line-1
        val afterLines = lines.drop(endLineIdx+1)
        val afterChars = lines(endLineIdx).drop(pos.column)
        //there is "annotation( .. )" but parser killed the closing ")" so add it again
        ")" + afterChars + "\n" + afterLines.mkString("\n")
      case NoAnnotation(pos) =>
        val endLineIdx = pos.line-1
        val afterLines = lines.drop(endLineIdx+1)
        val afterChars = lines(endLineIdx).drop(pos.column-1)
        //there is no annotation: generate closing the newly added annotation( .. )
        ");\n" + afterChars + "\n" + afterLines.mkString("\n")
    }
  }
}
