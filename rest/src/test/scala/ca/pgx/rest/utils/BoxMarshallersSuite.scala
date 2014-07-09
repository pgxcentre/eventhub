package ca.pgx.rest.utils

import akka.actor.ActorSystem
import net.liftweb.common.{Box, Full}
import org.specs2.mutable.Specification
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import spray.http._
import spray.httpx.marshalling._

class BoxMarshallersSuite extends Specification with BoxMarshallers {

  implicit val system = ActorSystem()

  "The Box Marshaller" should {
    "properly marshall an empty box instance to JSON" in {
      val expectedResponse = pretty(render(("status" -> "SUCCESSFUL")))
      // TODO: maybe there are better ways than comparing strings in this and other tests:
      marshal[Box[_]](Full(())) === Right(HttpEntity(ContentTypes.`application/json`, expectedResponse))
    }

    "properly marshall a full box instance to JSON" in {
      val expectedResponse = pretty(render(("status" -> "SUCCESSFUL") ~ ("value" -> "123") ))
      marshal[Box[_]](Full(123)) === Right(HttpEntity(ContentTypes.`application/json`, expectedResponse))
    }

    // TODO: add tests for all cases here
  }

}
