package ca.pgx.rest.utils

import java.text.SimpleDateFormat
import ca.pgx.common.events.ProcessingStatus._
import net.liftweb.common._
import spray.http.{HttpHeaders, HttpHeader, HttpEntity, ContentTypes}
import spray.httpx.LiftJsonSupport
import spray.httpx.marshalling.Marshaller

/**
 * Marshallers that will take of converting result type of [[Box]] to proper end-client
 * format.
 * ??? TODO: does it handle logging
 */
trait BoxMarshallers extends LiftJsonSupport {

  import net.liftweb.json._
  import net.liftweb.json.JsonDSL._

  implicit override def liftJsonFormats = new DefaultFormats {
    override def dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  }

  // TODO: override this and call the marshaller below...  implicit def liftJsonMarshaller[T <: AnyRef] =

  /**
   * Helps to simplify code within this trait - converts JSON to String for marshalling.
   * @param json
   * @return
   */
  implicit protected def jsonToString(json: JValue): String =
    pretty(render(json))

  /**
   * The convention we use is the following: ??? Box - smth to communicate
   *
   * This is similar to [[spray.httpx.marshalling.MetaMarshallers.optionMarshaller]] except that
   * we can communicate generic errors and specific error messages back.
   *
   * TODO: explain what HTTP status codes are returned, is it always 200 maybe? - it would be fine if user auth is done with reject for example
   *
   * Full - result of operation was successful and it should be communicated back to the client.
   * ParamFailure - an error happened and it should be communicated back to the client together with the param value.
   * Exception will not be communicated back because it may contain sensitive information and it's not so useful to
   * the client.
   * Failure - an error happened and it should be communicated back to the client.
   * Empty - an error happened but there is no specific message to pass back to the client. Prefer other
   * types of failure over this one.
   *
   * Additionally, based on types of values passed in Box subclasses we can convert some results to
   * others. For instance if Unit is returned the message should become empty.
   */
  implicit val ValidationResultMarshaller =
    Marshaller.of[Box[_]](/*ContentTypes.`text/plain(UTF-8)`, ContentTypes.`text/plain`,*/ ContentTypes.`application/json`) {
      (value, contentType, ctx) =>
        val responseDoc = value match {
          case Full(()) =>
            successResponse
          case Full(value) =>
            successResponse ~ responseValue(value)
          case ParamFailure(message, exception, _, value) =>
            // log exception: error(exception)
            failedResponse ~ responseMessage(message) ~ responseValue(value)
          case Failure(message, exception, _) =>
            // log exception: error...
            failedResponse ~ responseMessage(message)
          case Empty =>
            // log some error - debug
            failedResponse ~ responseMessage("Failure reason unknown, see logs.")
        }
        ctx.marshalTo(HttpEntity(contentType, responseDoc))
    }

  def response(result: ProcessingStatus): JObject =
    ("status" -> result.toString)

  def responseMessage(message: String): JObject =
    ("message" -> message)

  def responseValue(value: Any): JObject =
    ("value" -> value.toString)

  val successResponse = response(SUCCESSFUL)
  val failedResponse = response(FAILED)

}

// FIXME: compare to LiftJsonSupport - which one to use? examples:
//
//# curl localhost:8090/fullunit
//{"value":{}}#
//#
//# curl localhost:8090/full
//{"value":"something"}#
//#
//# curl localhost:8090/paramfail
//{"param":"PARAM"}#
//#
//# curl localhost:8090/fail
//{"msg":"failerr","exception":{},"chain":{}}#
//#
//# curl localhost:8090/empty
//{}#
//#

//https://github.com/spray/spray/blob/master/spray-httpx/src/test/scala/spray/httpx/marshalling/MetaMarshallersSpec.scala
//https://github.com/spray/spray/blob/master/spray-httpx/src/main/scala/spray/httpx/marshalling/MetaMarshallers.scala
