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
              println("RAW DATA PARSED AS JSON: " + reading)
              extractUserCredentials(reading) match {
                case scala.util.Success((apiUser, apiKey, project)) =>
                  println(apiUser + " -> " + apiKey)
                  authenticate(apiUser, apiKey, project) match {
                    // TODO: there might be more elegant ways in Spray dealing with Box
                    case Full((user, proj)) =>
                      val JArray(files) = reading \ "files"
                      val result = backup.processBackupReading(user, files, proj)
                      complete(result)
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
    }

}
