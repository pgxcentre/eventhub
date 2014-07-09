package ca.pgx.rest.service

import ca.pgx.rest.service.com.eventhub.rest.service.Boot

/**
 * This is the executable application itself.
 */
object EventHub extends App {

  Boot[SimpleRestServiceActor].start()
}