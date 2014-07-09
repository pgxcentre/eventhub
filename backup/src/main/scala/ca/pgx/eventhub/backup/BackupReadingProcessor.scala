package ca.pgx.eventhub.backup

import java.text.{SimpleDateFormat, DateFormat}
import java.util.Date
import ca.pgx.common.db.entities._
import ca.pgx.common.events.Validators.Validators
import ca.pgx.common.processors.AbstractFile
import ca.pgx.common.processors.Filters.Filters
import net.liftweb.common._
import net.liftweb.common.Box._
import net.liftweb.json.Formats
import net.liftweb.json._
import com.github.nscala_time.time.Imports._
import ca.pgx.common.events.EventType._
import ca.pgx.common.events.EventAction._
import org.bson.types.ObjectId
import com.foursquare.rogue.LiftRogue._
import ca.pgx.common._

trait BackupReadingProcessor {

  val defaultLiftDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

  implicit val formats = new DefaultFormats {
    override def dateFormatter = defaultLiftDateFormat // FIXME: is it a problem that it's not thread safe?
  }

  def processBackupReading(user: User, reading: JArray, project: Project): String = // FIXME: return type - what shall be communicated to the client?, HTTP reject, etc, maybe use a Box
    reading match {
      case JArray(files) =>
        val filez = files map convertToFile // TODO: check for errors, wrap in Try...
        validateBackupReading(filez, project) match {
          case Full(_) =>
            val msg = "Backup is good"
            onSuccess(msg, user.id.get, project.id.get)
            msg
          case Failure(errMsg, exc, _) =>
            // save
            val msg = s"Backup is bad, reason: [$errMsg]."
            onFailure(msg, user.id.get, project.id.get)
            msg
          case Empty =>
            val msg = "Backup is bad, reason: [UNKNOWN]."
            onFailure(msg, user.id.get, project.id.get)
            msg
        }
      case _ =>
        "Bad request"
    }

  /**
   * Expects fields with specific names and formats and converts JSON to a typesafe case class representation.
   * @param json
   * @return
   */
  def convertToFile(json: JValue): AbstractFile = {
    val processed = json transform {
      case JField("changedSecSinceEpoch", JInt(secs)) =>
        JField("dateModified", JString(defaultLiftDateFormat.format(new Date(secs.toLong * 1000L))))
    }
    processed.extract[AbstractFile]
  }

  def validateBackupReading(files: Traversable[AbstractFile], project: Project): ValidationResult = {
    val projId = project.id.get
    def getSettings =
      BackupProjectSettings.where(_.projectId eqs projId)
        .limit(1)
        .fetch()
        .headOption or
        Failure(s"Bad project configuration: project settings for project ID [$projId] not found!")
    for {
      settings <- getSettings
      _ = println("VALIDATION1-settings: " + settings.validate)
      _ = println("VALIDATION2-rules: " + settings.rules.validate + " validations: " + settings.rules.validations)
      _ = println("VALIDATION2-rules-each: " + settings.rules.get.map(_.validate))
      _ = println("VALIDATION3-validationsett: " + settings.rules.get.map(_.validations.validate))
      _ = println("VALIDATION4-validationsett-each: " + settings.rules.get.map(_.validations.get.map(_.validate)))
      rule <- settings.rules.valueBox or { // TODO: this validation might not be required since it's already done
        println(s"Bad project settings configuration [$settings].") // log it
        Failure("Bad project settings configuration.")
      }
      _ <- successOrFirstFailure(rule, applyRule, files)
    } yield ()
  }

  // TODO: move this to common proj like collection utils or smth:
  /**
   * Evaluates each receiver by applying transform on it with provided data. Returns first failure
   * encountered or success if no failures observed.
   * @param receivers
   * @param transform
   * @param data
   * @tparam T
   * @tparam U
   * @return
   */
  def successOrFirstFailure[T, U](receivers: Traversable[T], transform: (T, U) => ValidationResult, data: U): ValidationResult =
    receivers match {
      case h :: t =>
        val result = transform(h, data)
        if (result.isDefined)
          successOrFirstFailure(t, transform, data)
        else
          result
      case _ => SUCCESS
    }

  def applyRule(rule: Rule, files: Traversable[AbstractFile]): ValidationResult = {
    def getFilter: Filters =
      rule.filter.valueBox match {
        case Full(filter) =>
          filter.asInstanceOf[Filters]
        case _ =>
          println("BAD CONFIG!!!!!!!!!!!!!! " + rule) // log it, notify admin
          throw new IllegalStateException(s"Bad config for rule: [$rule].")
      }

    def getValidator(unapplied: ValidationSetting): Validators =
      unapplied.validation.valueBox.map(_.asInstanceOf[Validators])
        .getOrElse(throw new IllegalStateException(s"Bad config for validator settings: [$unapplied]."))

    // TODO: seems like I have to cast because of the way enums are implemented in Lift, maybe we can save case/any classes?
    // check that later. If type information is preserved except for Value type then we don't need to cast.
    // maybe if it's not available it can also be implemented.
    val filterFunction = getFilter
    val filteredFiles = filterFunction(rule.regex.get.r, files)
    val validators = rule.validations.get
    val validationFunction = (unapplied: ValidationSetting, files: Traversable[AbstractFile]) => {
      val func = getValidator(unapplied)
      println("VALID SETTING: " + unapplied.validate) // VALIDATION WORKS CORRECTLY HERE!!!!!!!!!!
      println("ARG VALUES: default" + unapplied.validationArg.defaultValue)
      println("ARG VALUES: defaultvalbox" + unapplied.validationArg.defaultValueBox)
      println("ARG VALUES: get" + unapplied.validationArg.get)
      println("ARG VALUES: itself" + unapplied.validationArg)
      println("ARG VALUES: validate" + unapplied.validationArg.validate)
      println("ARG VALUES: validations" + unapplied.validationArg.validations)
      val arg = unapplied.validationArg.get // TODO: this should be ok right? do I have to put everything in for compreh?
      println("ARG ARG ???!!!!: " + arg)
      func(arg, files)
    }
    successOrFirstFailure(validators, validationFunction, filteredFiles)
  }



  def onSuccess(msg: String, userId: ObjectId, projectId: ObjectId): Unit = {
    // FIXME: use one from project settings
    println("SUCCESS !!! SUCCESS !!! SUCCESS !!! " + msg)
    EventLog.createRecord
      .event(BACKUP)
      .userId(userId)
      .projectId(projectId)
      .when(DateTime.now.toDate)
      .alertRaised(false)
      .actionsTaken(LOG :: EMAIL :: Nil)
      .comment(msg)
      .save
  }

  def onFailure(msg: String, userId: ObjectId, projectId: ObjectId): Unit = {
    println("FAILURE !!! FAILURE !!! FAILURE !!! " + msg)
    EventLog.createRecord
      .event(BACKUP)
      .userId(userId)
      .projectId(projectId)
      .when(DateTime.now.toDate)
      .alertRaised(true)
      .actionsTaken(LOG :: EMAIL :: Nil)
      .comment(msg)
      .save
  }

}
