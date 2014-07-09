package ca.pgx.rest.service.authentication

import ca.pgx.common.db.entities.{Project, User}
import net.liftweb.common._
import net.liftweb.common.Box._
import com.foursquare.rogue.LiftRogue._
import net.liftweb.json.JsonAST.JString
import net.liftweb.json._
import org.bson.types.ObjectId
import scala.util.Try

/**
 * Authenticates API users.
 */
trait ApiUserAuth {

  // FIXME: needs to validate dates - see if the project is still active
  /**
   * Authenticates user via provided credentials and returns user back together with the project if found, or
   * error/empty if not found or not authenticated.
   *
   * @param apiUser
   * @param apiKey
   * @return
   */
  def authenticate(apiUser: String, apiKey: String, project: String): Box[(User, Project)] = {
    def authenticateUser =
      User.where(_.name eqs apiUser)
        .and(_.apiKey eqs apiKey)
        .limit(1)
        .fetch()
        .headOption or
        Failure("User not authenticated.")
    def authorizeUser(userId: ObjectId) =
      Project.where(_.name eqs project)
        .and(_.writers contains userId)
        .limit(1)
        .fetch()
        .headOption or
        Failure("User not authorized.")
    for {
      user <- authenticateUser
      proj <- authorizeUser(user.id.get)
    } yield (user, proj)
  }

  /**
   * Extracts API user name, key and project from the request.
   * @param json user request
   * @return a pair of (apiUser, apiKey, project)
   */
  def extractUserCredentials(json: JValue): Try[(String, String, String)] = Try {
    val JString(apiUser) = json \ "apiUser"
    val JString(apiKey) = json \ "apiKey"
    val JString(project) = json \ "project"
    (apiUser, apiKey, project)
  } recoverWith {
    case e =>
      // TODO: log failed attempts: debug(e)
      scala.util.Failure(new Exception("Bad request format: required attributes missing."))
  }
}
