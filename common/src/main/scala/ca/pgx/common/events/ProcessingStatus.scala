package ca.pgx.common.events

/**
 * A generic status of processing. Currently used to represent
 * status of processing a request - submission of data by client
 * to the project.
 */
object ProcessingStatus extends Enumeration {
  type ProcessingStatus = Value
  val SUCCESSFUL, FAILED = Value // TODO: need UNKNOWN ?
}
