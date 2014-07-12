package ca.pgx.eventhub.backup

import akka.actor.ActorSystem
import ca.pgx.common.events.EventAction
import net.liftweb.json.JsonAST.{JValue, JString}
import ca.pgx.common.db.entities.{EventLog, Project}
import scala.util._
import com.foursquare.rogue.LiftRogue._
import net.liftweb.common.Box

/**
 * TODO: ???
 */
object EventReceptionMonitor {

  println(EventAction.values)

  def checkEventsAreOnSchedule(): Unit = {
    import com.github.nscala_time.time.Imports._
    def isEventOnTime(project: Project) = {
      val lastEvent = EventLog.where(_.projectId eqs project.id.get)
        .orderDesc(_.when)
        .limit(1)
        .fetch()
        .headOption
      lastEvent exists {
        e =>
          val expectedSubmitInterval = project.submitIntervalSec.get.toInt.seconds
          new DateTime(e.when.get) isAfter DateTime.now - expectedSubmitInterval
      }
    }

    def getNotificationMessage(project: Project): Option[String] = { // TODO: change to JValue ???
      (project.isOnTime.get, isEventOnTime(project)) match {
        case (true, true) =>
          println("do nothing since it works")
          None
        case (true, false) =>
          project.isOnTime(false).save
          println("here we send the email that it failed")
          Some("here we send the email that it failed")
        case (false, true) =>
          println("here we send an email that its back")
          project.isOnTime(true).save
          Some("here we send an email that its back")
        case (false, false) =>
          println("do nothing since its still down")
          None
      }
    }

    // TODO: change Unit to Box? report errors
    for {
      project <- Project.findAll // validate???
      alerts <- project.lateSubmitAlerts.valueBox
      msg <- getNotificationMessage(project)
      alert <- alerts
      _ = EventAction(alert, msg)
    } ()
  }

  def init(system: ActorSystem): Unit = {
    import scala.concurrent.duration._
    implicit val ctx = system.dispatcher
    system.scheduler.schedule(5 seconds, 5 seconds) {
      Try {
        checkEventsAreOnSchedule()
      }
      match {
        case Success(s) => println("SUCCESS") // just log
        case Failure(e) => println("FAILED\n\n" + e + "\n\n") // email to admin and such
      }
    }
  }

}
