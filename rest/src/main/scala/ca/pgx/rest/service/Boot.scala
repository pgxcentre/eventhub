package ca.pgx.rest.service

package com.eventhub.rest.service

import akka.actor.{ActorLogging, Actor, ActorSystem, Props}
import akka.io.IO
import ca.pgx.common.communication.email.SmtpMailer
import spray.can.Http
import net.liftweb.util.{Props => LiftProps}
import scala.reflect.ClassTag
import scala.sys.SystemProperties
import net.liftweb.common.Full
import java.io.FileInputStream
import akka.event.LoggingAdapter
import ca.pgx.eventhub.backup.EventReceptionMonitor

/**
 * Entry point for the service. It runs as a standalone application and acts as an HTTP server.
 *
 * IMPORTANT: the initialization order here is important to avoid any data corruption.
 *
 * If you add an initialization step that can fail do it before web service binds to the port and starts servicing
 * requests if possible.
 *
 * If the service you are initializing does not protect you from race conditions you might be allowed to add
 * its initialization call after port bind. That way on a single machine port binding will serve as some sort
 * of lock which prevents multiple instances of this application running. Keep in mind that it's not a bulletproof
 * solution but usually is ok.
 *
 * Mongeez and Liquibase acquire database level locks and don't require any other synchronization mechanisms.
 */
final class Boot[RouteActor <: Actor with ActorLogging with RestService : ClassTag] private () extends BootDbInit { //CriticalExceptionHandlerImpl

  // FIXME: now Lift logging is used somehow
  implicit def log: LoggingAdapter = ??? //implicitly[RouteActor].log

  implicit lazy val system = ActorSystem("eventhub-actor-system")

  /**
   * Initializes all modules and starts up the service.
   */
  def start(): Unit = {
    initApp()
    initMail()
    initDb()
    initWebService()
    initEventMonitor()
  }

  protected def initApp(): Unit = {
    initProps()
    val banner = """
 _____                 _   _   _       _
| ____|_   _____ _ __ | |_| | | |_   _| |__
|  _| \ \ / / _ \ '_ \| __| |_| | | | | '_ \
| |___ \ V /  __/ | | | |_|  _  | |_| | |_) |
|_____| \_/ \___|_| |_|\__|_| |_|\__,_|_.__/
"""//.stripMargin
    info(banner)
    info(s"BOOT: Application is starting up. Running in [${LiftProps.mode}] mode.")
    sys.addShutdownHook(info("APPLICATION HAS BEEN SHUT DOWN.")) // TODO: check if it actually is able to print after shutdown, maybe use println?
  }

  protected def initProps(): Unit = {
    val props = new SystemProperties
    val filename = props.get("propsfile")
    filename foreach {
      f =>
        warn(s"OVERRIDING APPLICATION SETTINGS WITH SETTINGS FROM PROVIDED FILE: [$f] !")
        LiftProps.whereToLook = () => ((f, () => Full(new FileInputStream(f))) :: Nil)
    }
  }

  protected def initMail(): Unit = {
    doOrDie {
      SmtpMailer.init
      info("SMTP mail sender initialized.")
    }
  }

  protected def initEventMonitor(): Unit = {
    EventReceptionMonitor.init(system)
  }

  protected def initWebService(): Unit = {
    val restPort = LiftProps.getInt("service.restPort", 8090)
    lazy val service = system.actorOf(Props[RouteActor], "eventhub-rest-service-actor")
    IO(Http) ! Http.Bind(service, interface = LiftProps.get("interface", "0.0.0.0"), port = restPort)
    info(s"Started listening to web requests on port [$restPort].")
    info("BOOT: Application startup is complete. Ready to receive requests.")
  }
}

/**
 * Instances of this service can be created via this companion object only. This forces clients to provide correct
 * type arguments.
 */
object Boot {
  def apply[RouteActor <: Actor with ActorLogging with RestService : ClassTag](): Boot[RouteActor] =
    new Boot[RouteActor]
}
