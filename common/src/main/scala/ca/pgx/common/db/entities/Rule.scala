package ca.pgx.common.db.entities

import ca.pgx.common.db.helpers.CustomBsonMetaRecord
import ca.pgx.common.events.EventAction._
import ca.pgx.common.processors.Filters
import net.liftweb.mongodb.record.BsonRecord
import net.liftweb.mongodb.record.field.{MongoListField, BsonRecordListField}
import net.liftweb.record.field.{StringField, EnumNameField}

/**
 * A single rule that will be applied for each file.
 */
class Rule extends BsonRecord[Rule] {
  override def meta = Rule

  /**
   * a filter function that should be invoked on a list of entries (files).
   * See [[Filters]] apply functions that perform filtering.
   */
  object filter extends EnumNameField(this, Filters)

  /**
   * Regex that will be passed to filter functions.
   */
  object regex extends StringField(this, 128)

  /**
   * List of validations that will be applied to each element returned from filter. Validations
   * will be applied in the order they are defined here.
   */
  object validations extends BsonRecordListField(this, ValidationSetting) {
    /**
     * Validate all elements of the list by default which is not done by Lift now.
     * TODO: this can be abstracted into a trait that is mixed in at Field level or at MetaRecord level like jsonifiable trait.
     * @return
     */
    override def validations = ((elems: ValueType) => elems.map(_.validate).flatten) :: super.validations
  }

  /**
   * List of actions that will be perfomed on successful operation. Usually this would
   * be empty, but logging and emailing can be enabled for debugging.
   */
  object onSuccess extends MongoListField[Rule, EventAction](this)

  /**
   * List of actions that will be perfomed on failed operation. Usually this would
   * include logging and emailing.
   */
  object onFailure extends MongoListField[Rule, EventAction](this)

}

object Rule extends Rule with CustomBsonMetaRecord[Rule]
