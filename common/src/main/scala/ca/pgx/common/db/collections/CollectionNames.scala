package ca.pgx.common.db.collections

/**
 * This is a list of all collection names used in MongoDB.
 *
 * DAOs and DB classes should reference these constants for their names.
 * The intention is to keep all collection names in a single place.
 * These names will be used by DB utils, unit tests, and such.
 *
 * Naming convention for collection names is "CamelCase". Notice that
 * collection names, and field names in mongodb are case sensitive.
 */
object CollectionNames extends Enumeration {
  type CollectionNames = Value

  val EVENT_LOG = Value("eventLog")

  val USER = Value("user")

  val PROJECT = Value("project")

  val BACKUP_PROJECT_SETTINGS = Value("backupProjectSettings")
}
