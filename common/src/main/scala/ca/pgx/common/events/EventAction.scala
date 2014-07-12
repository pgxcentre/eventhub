package ca.pgx.common.events

import ca.pgx.common.mixins.callback.EnumeratedFunction
import net.liftweb.json.JsonAST.JValue

/**
 * Type of action taken when event was processed.
 */
object EventAction extends EnumeratedFunction[String, Unit] {

  type EventAction = Value

  val EMAIL, LOG, REST_CALL, OS_CMD = Value

  override val functionMappings =
    Map(
      EMAIL -> ((arg: String) => println("FROM EMAIL: " + arg)),
      LOG -> ((arg: String) => println("FROM LOG: " + arg))
    )

  override def apply(enum: Value, arg: String): Unit =
    functionMappings.getOrElse(enum, (arg: String) => ())(arg)
}
