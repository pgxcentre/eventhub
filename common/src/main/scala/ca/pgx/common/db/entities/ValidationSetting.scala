package ca.pgx.common.db.entities

import ca.pgx.common.db.helpers.CustomBsonMetaRecord
import ca.pgx.common.events.Validators
import net.liftweb.common.Empty
import net.liftweb.mongodb.record.BsonRecord
import net.liftweb.record.field.{LongField, EnumNameField}

/**
 * Stores/maps to a function that will be applied as a data validation
 * together with an argument for that function.
 */
class ValidationSetting extends BsonRecord[ValidationSetting] {
  override def meta = ValidationSetting

  /**
   * Validation function.
   */
  object validation extends EnumNameField(this, Validators) {
    //override def optional_? = false
    //override def defaultValueBox = Empty
  }

  // TODO: this allows only Long/Int args at the moment, see Validators enum for implementation of
  // apply which takes that argument. Can we make this more flexible and store a list of args/tuple?
  /**
   * Argument that should be passed to a validation function.
   * See the exact validation function for the type of units used (bytes, secs, etc).
   */
  object validationArg extends LongField(this) {
    override def optional_? = false
    override def defaultValueBox = Empty
  }
}

object ValidationSetting extends ValidationSetting with CustomBsonMetaRecord[ValidationSetting]
