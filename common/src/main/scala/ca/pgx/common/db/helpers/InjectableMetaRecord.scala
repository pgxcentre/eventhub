package ca.pgx.common.db.helpers

import ca.pgx.common.db.connection.MongoConfig
import net.liftweb.mongodb.record._

/**
 * A custom MongoMetaRecord that adds an injectable MongoIdentifier and helper methods.
 */
trait InjectableMetaRecord[A <: MongoRecord[A]] extends /*JsonifiableRecord[A] with*/ MongoMetaRecord[A] {
  this: A =>

  override def mongoIdentifier = MongoConfig.identifier.vend
}
