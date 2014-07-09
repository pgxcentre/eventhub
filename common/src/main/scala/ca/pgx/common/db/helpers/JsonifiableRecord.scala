package ca.pgx.common.db.helpers

/*
import net.liftweb.common.{Empty, Box, Full, ParamFailure}
import scala.util._
import net.liftweb.mongodb.record.MongoRecord
import ca.pgx.common.results.ResultAndErrorHandlingHelper._
import ca.pgx.common.results.ClientError

/**
 * Adds JSON convenience methods to regular Mongo Record classes.
 */
trait JsonifiableRecord[U <: MongoRecord[U]] extends MongoRecord[U] {
  this: U =>

  /**
   * Parses a JSON String, validates it and saves it to DB if it's a correctly formed document.
   *
   *  @return An entity that corresponds to the saved document in DB (with ID assigned), or
   *  an error.
   */
  def saveJsonDb(implicit jsonStr: String): Box[U] =
    for {
      parsedDoc <- parseJson(jsonStr)
      _ <- validateFields(parsedDoc)
      docDb <- parsedDoc.saveTheRecord
    } yield docDb

  def parseJson(jsonStr: String): Box[U] =
    Try {
      meta.fromJsonString(jsonStr)
    } match {
      case Success(v) => v
      case Failure(err) =>
        val response = ClientError("Malformed JSON format", err)
        EXTERNAL_FAILURE(response, Full(err), "msg.filter.wrong.json.format")
        //Empty // FIXME later - proper errors should be returned
    }

  def validateFields(doc: U): Box[Unit] =
    doc validate match {
      case Nil => SUCCESS
      case errors =>
        EXTERNAL_FAILURE(ClientError(errors))
    }
}
*/