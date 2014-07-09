package ca.pgx

import ca.pgx.common.processors.AbstractFile
import net.liftweb.common.{Full, Box}

/**
 * Common types and constants that should be easily accessible.
 *
 * Simply import {{{import ca.pgx.common._}}} to have them all be accessible in the scope.
 */
package object common {

  type Files = Traversable[AbstractFile] // TODO: move to package object in common together with other common types like AbstractFile, SUCCESS

  type ValidationResult = Box[Unit]

  val SUCCESS: ValidationResult = Full(())
}
