package ca.pgx.eventhub.backup

import akka.actor.ActorSystem
import ca.pgx.common.events.EventAction
import net.liftweb.json.JsonAST.JString
import scala.concurrent.duration._
import ca.pgx.common.db.entities.Project
import ca.pgx.common.events.EventAction.EventAction
import net.liftweb.json.JsonDSL._
import scala.util._

/**
 * TODO: ???
 */
object EventReceptionMonitor {

  println(EventAction.values)

  def checkEventsAreOnSchedule(): Unit = { // TODO: change Unit to Box? report errors
    for {
      project <- Project.findAll // validate???
      _ = println("PROJECT: " + project)
      alerts <- project.lateSubmitAlerts.valueBox
      _ = println("ALERTS: " + alerts)
      alert <- alerts
      _ = println("ALERT: " + alert)
      _ = EventAction(alert, JString("Dummy string")) // TODO: nicer more generic syntax?
    } ()
  }

  def init(system: ActorSystem): Unit = {
    implicit val ctx = system.dispatcher
    system.scheduler.schedule(5 seconds, 5 seconds) {
      Try { checkEventsAreOnSchedule() }
      match {
        case Success(s) => println("SUCCESS") // just log
        case Failure(e) => println("FAILED\n\n" + e + "\n\n") // email to admin and such
      }
    }
  }

}
