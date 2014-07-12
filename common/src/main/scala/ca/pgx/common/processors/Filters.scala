package ca.pgx.common.processors

import ca.pgx.common.mixins.callback.EnumeratedFunction
import net.liftweb.common._
import org.joda.time.DateTime
import ca.pgx.common._

import scala.util.matching.Regex

/**
 * Implementation of filters that can be applied to a list of
 * values like files. These filters can be stored in DB, read
 * and applied in a more natural way than mapping a string to
 * Scala code.
 */
object Filters extends EnumeratedFunction[(Regex, Files), Files] {
  // TODO: perhaps files arg should be abstracted to much more general type String like

  type Filters = Value

  val FILTER, FILTER_NOT = Value

  private def flt(regex: Regex, files: Files) =
    files filter { f => (regex findFirstIn f.filename) isDefined}

  private def fltNot(regex: Regex, files: Files) =
    files filterNot { f => (regex findFirstIn f.filename) isDefined}

  override val functionMappings =
    Map(
      FILTER -> (flt _).tupled,
      FILTER_NOT -> (fltNot _).tupled
    )

  override def apply(enum: Value, arg: (Regex, Files)): Files =
    functionMappings.getOrElse(enum, (arg: (Regex, Files)) => arg._2)(arg)

}
