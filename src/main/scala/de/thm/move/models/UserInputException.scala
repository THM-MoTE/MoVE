package de.thm.move.models

/** Used for illegal inputs from the User.
 * Exceptions should be handled within the Try-Monad!
 */
case class UserInputException(msg:String) extends IllegalArgumentException
