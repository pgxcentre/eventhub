package ca.pgx.common.mixins.callback

import net.liftweb.json.JsonAST.JValue

/**
 * ???
 */
trait EnumeratedFunction extends Enumeration {

  protected def functionMappings: Map[Value, Function1[JValue, Unit]]

  def apply(enum: Value, arg: JValue): Unit =
    functionMappings.getOrElse(enum, (arg: JValue) => ())(arg)
}
