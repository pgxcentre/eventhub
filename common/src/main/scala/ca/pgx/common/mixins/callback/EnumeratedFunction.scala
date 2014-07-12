package ca.pgx.common.mixins.callback

/**
 * A generic marker trait that adds function-like behavior to enums.
 *
 * We have a need to lookup enums from db which represent certain actions.
 * Once looked up a behavior mapped to the enum needs to be executed.
 */
trait EnumeratedFunction[T, R] extends Enumeration {

  // TODO: make it a partial function or document that exceptions can be thrown
  protected def functionMappings: Map[Value, Function1[T, R]]

  def apply(enum: Value, arg: T): R
}
