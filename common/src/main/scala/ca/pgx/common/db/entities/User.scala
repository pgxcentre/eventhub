package ca.pgx.common.db.entities

import ca.pgx.common.db.collections.CollectionNames
import ca.pgx.common.db.helpers.InjectableMetaRecord
import com.foursquare.index.IndexedRecord
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.StringField

/**
 * Users or clients in the system.
 */
class User extends MongoRecord[User] with ObjectIdPk[User] with IndexedRecord[User] {

  override def meta = User

  /**
   * Username.
   */
  object name extends StringField(this, 256)

  /**
   * Authentication key that is required when user interacts with the API.
   */
  object apiKey extends StringField(this, 256)
}

object User extends User with InjectableMetaRecord[User] {
  override def collectionName = CollectionNames.USER.toString
}
