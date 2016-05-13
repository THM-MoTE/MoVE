/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.util

sealed trait Validation[+A, +E] {
  def map[B](fn: A => B): Validation[B, E]
  def flatMap[B, EE >: E](fn: A => Validation[B,EE]): Validation[B, EE]
  def getValue:A
  def getWarnings:List[E]
}

case class ValidationSuccess[A](value:A) extends Validation[A, Nothing] {
  def map[B](fn: A => B): Validation[B, Nothing] = ValidationSuccess(fn(value))
  def flatMap[B, EE >: Nothing](fn: A => Validation[B,EE]): Validation[B, EE] = fn(value)
  def getValue:A = value
  def getWarnings:List[Nothing] = Nil
}
case class ValidationWarning[A, E](value:A, warnings:List[E]) extends Validation[A, E] {
  def map[B](fn: A => B): Validation[B, E] = ValidationWarning(fn(value), warnings)
  def flatMap[B, EE >: E](fn: A => Validation[B,EE]): Validation[B, EE] = fn(value) match {
    case ValidationSuccess(v) => ValidationWarning(v, warnings)
    case ValidationWarning(v, xs) => ValidationWarning(v, warnings ++ xs)
  }
  def getValue:A = value
  def getWarnings:List[E] = warnings
}

object Validation {
  def apply[A, E](a:A):Validation[A,E] = ValidationSuccess(a)
}
object ValidationWarning {
  def apply[A, E](a:A, w:E):ValidationWarning[A,E] = ValidationWarning(a, List(w))
}
