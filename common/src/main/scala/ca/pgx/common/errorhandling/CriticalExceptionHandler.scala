package ca.pgx.common.errorhandling

import scala.util._
import scala.util.control.NonFatal

/**
 * Helper methods for handling exceptions in the application.
 */
trait CriticalExceptionHandler {

  /**
   * Runs code block and if any exception is encountered exits the application.
   * This is useful for critical initialization steps in the application. Don't abuse it otherwise.
   * @param block
   */
  def doOrDie(block: => Unit): Unit
}

/**
 * @inheritdoc
 */
trait CriticalExceptionHandlerImpl extends CriticalExceptionHandler {

  /**
   * @inheritdoc
   */
  override def doOrDie(block: => Unit): Unit =
    try {
      println("DoOrDie: Started executing critical code section.")
      block
      println("DoOrDie: Successful Operation.")
    } catch {
      case NonFatal(e) =>
        val err = e.getStackTrace.mkString("\n")
        sys.error(s"DoOrDie: Failed with: [$err]. Exiting application.")
        sys.exit(1)
    }
}
