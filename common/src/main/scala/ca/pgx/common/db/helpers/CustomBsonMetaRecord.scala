package ca.pgx.common.db.helpers

import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord}

/**
 * A [[BsonMetaRecord]] type that allows us to define custom representation
 * and processing for meta records. For example, we can mix in pretty json class or
 * override default JSON rendering.
 *
 * @tparam A
 */
trait CustomBsonMetaRecord[A <: BsonRecord[A]] extends BsonMetaRecord[A] { //with PrettyJson[A] {
  this: A =>
}