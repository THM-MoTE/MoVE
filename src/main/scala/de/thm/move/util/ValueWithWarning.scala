/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.util

sealed trait ValueWithWarning[+A, +E] {
  def map[B](fn: A => B): ValueWithWarning[B, E]
  def flatMap[B, EE >: E](fn: A => ValueWithWarning[B,EE]): ValueWithWarning[B, EE]
  def getValue:A
  def getWarnings:List[E]
}

case class ValueSuccess[A](value:A) extends ValueWithWarning[A, Nothing] {
  def map[B](fn: A => B): ValueWithWarning[B, Nothing] = ValueSuccess(fn(value))
  def flatMap[B, EE >: Nothing](fn: A => ValueWithWarning[B,EE]): ValueWithWarning[B, EE] = fn(value)
  def getValue:A = value
  def getWarnings:List[Nothing] = Nil
}
case class ValueWarning[A, E](value:A, warnings:List[E]) extends ValueWithWarning[A, E] {
  def map[B](fn: A => B): ValueWithWarning[B, E] = ValueWarning(fn(value), warnings)
  def flatMap[B, EE >: E](fn: A => ValueWithWarning[B,EE]): ValueWithWarning[B, EE] = fn(value) match {
    case ValueSuccess(v) => ValueWarning(v, warnings)
    case ValueWarning(v, xs) => ValueWarning(v, warnings ++ xs)
  }
  def getValue:A = value
  def getWarnings:List[E] = warnings
}

object ValueWithWarning {
  def apply[A, E](a:A):ValueWithWarning[A,E] = ValueSuccess(a)
}
object ValueWarning {
  def apply[A, E](a:A, w:E):ValueWarning[A,E] = ValueWarning(a, List(w))
}
