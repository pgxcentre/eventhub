package ca.pgx.rest.service

import java.text.SimpleDateFormat

import ca.pgx.common.db.entities.{ValidationSetting, Rule, BackupProjectSettings, Project}
import ca.pgx.common.events.{Validators, EventAction}
import ca.pgx.common.processors.Filters
import ca.pgx.eventhub.backup.BackupReadingProcessor
import ca.pgx.rest.service.authentication.ApiUserAuth
import ca.pgx.rest.utils.BoxMarshallers
import net.liftweb.common._
import net.liftweb.json.JsonAST.{JArray, JValue}
import org.bson.types.ObjectId
import org.joda.time.DateTime
import spray.httpx.{LiftJsonSupport, SprayJsonSupport}
import spray.routing.AuthenticationFailedRejection.CredentialsRejected
import spray.routing._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

/*trait RestService extends HttpService {
  def route: Route
}
*/
trait RestService extends HttpService with BoxMarshallers with ApiUserAuth {
  //with SprayJsonSupport {

  val backup = new BackupReadingProcessor {}

  // TODO: use json find later for all requests and reject route if not found

  // TODO: cleanup code by using Umarshallers and avoid case matches

  def route =
    path("api" / "event") {
      get {
        complete("these are all events")
      } ~
        post {
          entity(as[JValue]) {
            reading =>
              //createProjectConfig() // FIXME: remove this later, it's just for testing

              println("RAW DATA PARSED AS JSON: " + reading)
              extractUserCredentials(reading) match {
                case scala.util.Success((apiUser, apiKey, project)) =>
                  println(apiUser + " -> " + apiKey)
                  authenticate(apiUser, apiKey, project) match {
                    // TODO: there might be more elegant ways in Spray dealing with Box
                    case Full((user, proj)) =>
                      val JArray(files) = reading \ "files"
                      val msg = backup.processBackupReading(user, files, proj)
                      complete(s"event posted SUCCESSFULLY!!!, result of processing is [$msg].")
                    case failedAuthErr =>
                      println(failedAuthErr)
                      reject(AuthenticationFailedRejection(CredentialsRejected, Nil))
                  }
                case scala.util.Failure(err) =>
                  println(err)
                  reject(AuthenticationFailedRejection(CredentialsRejected, Nil)) // TODO: give more specific error - bad format
              }
          }
        }
    } ~
      //path("test") { // import implicit marshaller for the BOX - now using LIFT JSON support
        get {
          path("fullunit") {
            complete(Full(()))
          } ~
            path("full") {
              complete(Full("something"))
            } ~
            path("paramfail") {
              complete(ParamFailure("msg", "PARAM"))
            } ~
            path("fail") {
              complete(Failure("failerr"))
            } ~
            path("empty") {
              complete(Empty)
            }
        }
      //}

  def createProjectConfig(): Unit = {
    println("CREATING PROJ CONFIG")

    // PROJECT
    val projId = new ObjectId()
    Project.createRecord
      .id(projId)
      .name("backupmonitor")
      .startDate(DateTime.now.toDate)
      .writers(new ObjectId("53a0590a355b6c1f89e721a2") :: Nil)
      .save

    // VALIDATIONS
    val sizeValidation = ValidationSetting.createRecord
      .validation(Validators.MIN_SIZE)
      .validationArg(1)
    val countValidation = ValidationSetting.createRecord
      .validation(Validators.COUNT)
      .validationArg(1)

    // RULE
    val mysqlDumpRegex = """^.+\.sql$"""
    val sizeRule = Rule.createRecord
      .filter(Filters.FILTER)
      .regex(mysqlDumpRegex)
      .validations(sizeValidation :: Nil)
      .onSuccess(EventAction.LOG :: Nil)
      .onFailure(EventAction.LOG :: EventAction.EMAIL :: Nil)
    val countRule = Rule.createRecord
      .filter(Filters.FILTER)
      .regex(mysqlDumpRegex)
      .validations(countValidation :: Nil)
      .onSuccess(EventAction.LOG :: Nil)
      .onFailure(EventAction.LOG :: EventAction.EMAIL :: Nil)

    // PROJECT SETTINGS
    BackupProjectSettings.createRecord
      .projectId(projId)
      .rules(sizeRule :: countRule :: Nil)
      .save
    // more fields later
  }


}
