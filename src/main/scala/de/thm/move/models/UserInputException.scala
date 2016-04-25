/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.models

/** Used for illegal inputs from the User.
 * Exceptions should be handled within the Try-Monad!
 */
case class UserInputException(msg:String) extends IllegalArgumentException
