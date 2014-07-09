package ca.pgx.common.events

/**
 * Types of events that can be processed by the system.
 */
object EventType extends Enumeration {
  type EventType = Value
  val BACKUP, RESOURCE_MONITOR = Value
}
