package ca.pgx.common.db.entities

import ca.pgx.common.db.collections.CollectionNames
import ca.pgx.common.db.helpers.InjectableMetaRecord
import ca.pgx.common.events.EventAction.EventAction
import ca.pgx.common.events.EventType
import com.foursquare.index.IndexedRecord
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.field.{DateField, MongoListField, ObjectIdPk, ObjectIdRefField}
import net.liftweb.record.field.{EnumNameField, BooleanField, EnumField, StringField}

/**
 * A Log of all events handled by the service with processed status.
 *
 * If you want this collection to be capped you can create it beforehand - before the application
 * runs for the first time.
 */
class EventLog extends MongoRecord[EventLog] with ObjectIdPk[EventLog] with IndexedRecord[EventLog] {

  override def meta = EventLog

  /**
   * Id of a user who submitted the reading.
   */
  object userId extends ObjectIdRefField(this, User)

  /**
   * Project to which this entry belongs.
   */
  object projectId extends ObjectIdRefField(this, Project)

  /**
   * Type of an event registered.
   */
  object event extends EnumNameField(this, EventType)

  /**
   * When this event was registered.
   */
  object when extends DateField(this)

  /**
   * A boolean that takes the value of true if an alert was raised. This flag is used for alerts
   * only and not notifications. For example if backup validation fails we send an alert by email,
   * while on successful backup if users are interested we can send a notification email.
   * Any failed validation should raise an alert because otherwise there is no point in monitoring it.
   */
  object alertRaised extends BooleanField(this)

  /**
   * A list of actions taken in the order of processing.
   */
  object actionsTaken extends MongoListField[EventLog, EventAction](this)

  /**
   * Optional comment written by Processors. Might contain commands executed, human readable text, etc.
   */
  object comment extends StringField(this, 2048)

}

object EventLog extends EventLog with InjectableMetaRecord[EventLog] {
  override def collectionName = CollectionNames.EVENT_LOG.toString
}
