package ca.pgx.eventhub.backup

import java.text.{SimpleDateFormat, DateFormat}
import java.util.Date
import ca.pgx.common.db.entities._
import ca.pgx.common.events.{EventAction, Validators}
import ca.pgx.common.events.Validators.Validators
import ca.pgx.common.events.Validators.Validators
import ca.pgx.common.processors.{Filters, AbstractFile}
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

  def processBackupReading(user: User, reading: JArray, project: Project): ValidationResult = {
    def convertReadingFormat =
      scala.util.Try {
        reading.arr map convertToFile
      } match {
        case scala.util.Success(files) => Full(files)
        case scala.util.Failure(e) => Failure("Bad request format")
      }

    def processResult(result: ValidationResult, settings: BackupProjectSettings, userId: ObjectId): Unit = {
      val logEntry = EventLog.createRecord
        .event(BACKUP)
        .userId(userId)
        .projectId(settings.projectId.get)
        .when(DateTime.now.toDate)

      result match {
        case SUCCESS =>
          logEntry.alertRaised(false)
            .comment("Successful")
            .actionsTaken(settings.onSuccess.get)
            .save
          settings.onSuccess.get foreach {
            EventAction(_, "Event was submitted successfully")
          }
        case err =>
          val msg = (err ?~ "").toString //???? FIXME: extract message properly via case match
          logEntry.alertRaised(true)
            .comment(msg)
            .actionsTaken(settings.onFailure.get)
            .save
          settings.onFailure.get foreach {
            EventAction(_, msg)
          }
      }
    }

    for {
      convFiles <- convertReadingFormat
      backupSettings <- getSettings(project.id.get)
      result = validateBackupReading(convFiles, project, backupSettings)
      _ = processResult(result, backupSettings, user.id.get)
    } yield result
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

  def getSettings(projectId: ObjectId) = {
    val settings = BackupProjectSettings.where(_.projectId eqs projectId)
      .limit(1)
      .fetch()
      .headOption or
      Failure(s"Bad project configuration: project settings for project ID [$projectId] not found!")
    def validateSettings(sett: BackupProjectSettings) =
      sett.validate match {
        case h :: t => Failure(h.msg.toString) // FIXME: different type of failure??? - do not report to the client
        case _ => SUCCESS
      }
    for {
      sett <- settings
      _ <- validateSettings(sett)
    } yield sett
  }

  def validateBackupReading(files: Traversable[AbstractFile], project: Project, settings: BackupProjectSettings): ValidationResult =
    for {
      rule <- settings.rules.valueBox
      _ <- successOrFirstFailure(rule, applyRule, files)
    } yield ()

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
    def validationFunction(unapplied: ValidationSetting, files: Traversable[AbstractFile]) = {
      val func = unapplied.validation.get
      val arg = unapplied.validationArg.get
      Validators(func, (arg, files))
    }

    for {
      filterFunction <- rule.filter.valueBox
      filteredFiles = Filters(filterFunction, (rule.regex.get.r -> files))
      validators <- rule.validations.valueBox
      _ <- successOrFirstFailure(validators, validationFunction, filteredFiles)
    } yield ()
  }

}
