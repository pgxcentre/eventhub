package ca.pgx

import ca.pgx.common.processors.AbstractFile
import net.liftweb.common.{Failure, ParamFailure, Full, Box}

/**
 * Common types and constants that should be easily accessible.
 *
 * Simply import {{{import ca.pgx.common._}}} to have them all be accessible in the scope.
 */
package object common {

  type Files = Traversable[AbstractFile]

  type ValidationResult = Box[Unit]

  val SUCCESS: ValidationResult = Full(())

  /**
   * Helps to extract an error message from a type of a failure to avoid extensive pattern matching.
   * To supply default error message use together with `?~` operator, for example:
   * {{{
   *   val error = Empty
   *   getErrorMessage(error ?~ "Default error message here")
   * }}}
   * @return
   */
  def getErrorMessage: PartialFunction[Box[_], String] = {
    case ParamFailure(msg, _, _, _) => msg
    case Failure(msg, _, _) => msg
  }
}
