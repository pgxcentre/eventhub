package ca.pgx.common.events

import ca.pgx.common.mixins.callback.EnumeratedFunction
import net.liftweb.json.JsonAST.JValue

/**
 * Type of action taken when event was processed.
 */
object EventAction extends EnumeratedFunction {

  //  type EventAction = Val with Function1[JValue, Unit]
  //
  //  val EMAIL = new Val with Function1[JValue, Unit] {
  //    override val toString = "EMAIL"
  //
  //    override def apply(message: JValue): Unit = println("the message is : " + message)
  //  }
  //
  //  val LOG = new Val with Function1[JValue, Unit] {
  //    override val toString = "LOG"
  //
  //    override def apply(message: JValue): Unit = println("this is log" + message)
  //  }
  //
  //  val REST_CALL = new Val with Function1[JValue, Unit] {
  //    override val toString = "REST_CALL"
  //
  //    override def apply(message: JValue): Unit = ???
  //  }
  //
  //  val OS_CMD = new Val with Function1[JValue, Unit] {
  //    override val toString = "OS_CMD"
  //
  //    override def apply(message: JValue): Unit = ???
  //  }

  type EventAction = Value

  val EMAIL, LOG, REST_CALL, OS_CMD = Value

  //override val functionMappings = Map()

  override val functionMappings: Map[EventAction, Function1[JValue, Unit]] =
    Map(
      EMAIL -> ((arg: JValue) => println("FROM EMAIL: " + arg)),
      LOG -> ((arg: JValue) => println("FROM LOG: " + arg))
    )
  //override protected val functionMappings: Map[Value, (JValue) => Unit] = Map()
}

