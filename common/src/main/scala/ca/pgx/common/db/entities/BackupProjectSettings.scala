package ca.pgx.common.db.entities

import ca.pgx.common.db.collections.CollectionNames
import ca.pgx.common.db.helpers.{CustomBsonMetaRecord, InjectableMetaRecord}
import ca.pgx.common.events.{Validators, EventAction}
import ca.pgx.common.events.EventAction.EventAction
import ca.pgx.common.processors.{AbstractFile, Filters}
import com.foursquare.index.IndexedRecord
import net.liftweb.mongodb.record.{BsonRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{ObjectIdRefField, MongoListField, BsonRecordListField, ObjectIdPk}
import net.liftweb.record.field.{EnumNameField, LongField, EnumField, StringField}
import net.liftweb.util.FieldError

/**
 * Settings for a backup type project. These are kept separately from the project because
 * project can be used for many other types of events.
 * There must be one backup project settings per each project, i.e. 1 to 1 relationship.
 */
class BackupProjectSettings extends MongoRecord[BackupProjectSettings] with ObjectIdPk[BackupProjectSettings]
with IndexedRecord[BackupProjectSettings] {

  override def meta = BackupProjectSettings

  /**
   * Project to which this config belongs.
   */
  object projectId extends ObjectIdRefField(this, Project)

  /**
   * List of rules that will be applied to received data.
   */
  object rules extends BsonRecordListField(this, Rule) {
    /**
     * Validate all elements of the list by default which is not done by Lift now.
     * TODO: this can be abstracted into a trait that is mixed in at Field level or at MetaRecord level like jsonifiable trait.
     * related questions:
     * https://groups.google.com/forum/#!msg/liftweb/gh2syNeGWyw/OC3y2TLWfAkJ
     * http://stackoverflow.com/questions/24368505/lift-record-does-not-validate-list-elements-when-calling-validate-bug-or-fea
     * @return
     */
    override def validations = ((elems: ValueType) => elems.map(_.validate).flatten) :: super.validations
  }

  /**
   * List of actions that will be perfomed on successful operation. Usually this would
   * be empty, but logging and emailing can be enabled for debugging.
   */
  object onSuccess extends MongoListField[BackupProjectSettings, EventAction](this) // FIXME: add custom serialization to all MongoListField fields - throws otherwise

  /**
   * List of actions that will be perfomed on failed operation. Usually this would
   * include logging and emailing.
   */
  object onFailure extends MongoListField[BackupProjectSettings, EventAction](this)
}

object BackupProjectSettings extends BackupProjectSettings with InjectableMetaRecord[BackupProjectSettings] {
  override def collectionName = CollectionNames.BACKUP_PROJECT_SETTINGS.toString
}
