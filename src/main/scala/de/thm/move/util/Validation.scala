/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.util

/** A monad for calculating values which could return a value with warnings.
  * @tparam A the type of the value
  * @tparam E the type of the warning
  */
sealed trait Validation[+A, +E] {
  /** Maps the value with the given function fn, copying the warnings from this Validation to the
    * returning Validation.
    */
  def map[B](fn: A => B): Validation[B, E]
  /** Combines this Validation with the Validation returned from calling fn.
    *
    * fn is called with the value from this Validation. If this Validation and the returning Validation
    * from fn contains warnings they will be joined.
    */
  def flatMap[B, EE >: E](fn: A => Validation[B,EE]): Validation[B, EE]
  /** Returns the value of this Validation */
  def getValue:A
  /** Returns the warnings of this Validation */
  def getWarnings:List[E]
}

/** A validation without a warning. */
case class ValidationSuccess[A](value:A) extends Validation[A, Nothing] {
  def map[B](fn: A => B): Validation[B, Nothing] = ValidationSuccess(fn(value))
  def flatMap[B, EE >: Nothing](fn: A => Validation[B,EE]): Validation[B, EE] = fn(value)
  def getValue:A = value
  def getWarnings:List[Nothing] = Nil
}
/** A validation with warnings. */
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
  /** Returns a ValidationSuccess with the given value a */
  def apply[A, E](a:A):Validation[A,E] = ValidationSuccess(a)
}
object ValidationWarning {
  /** Returns a ValidationWarning with the given value a and the given warning w wrapped inside a list. */
  def apply[A, E](a:A, w:E):ValidationWarning[A,E] = ValidationWarning(a, List(w))
}
