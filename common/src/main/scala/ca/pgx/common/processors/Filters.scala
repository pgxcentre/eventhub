package ca.pgx.common.processors

import net.liftweb.common._
import org.joda.time.DateTime
import ca.pgx.common._

import scala.util.matching.Regex

// FIXME: fix this enum later, perhaps it's not implemented
// in the best way possible in terms of manually defining
// id and name/toString, however it should have apply method
// which will be naturally used by Processors.

/**
 * Implementation of filters that can be applied to a list of
 * values like files. These filters can be stored in DB, read
 * and applied in more natural way than matching string to
 * Scala code.
 */
// TODO: perhaps files arg should be abstracted to much more general type String like
object Filters extends Enumeration with Function2[Regex, Files, Files] {
  override def apply(v1: Regex, v2: Files): Files =
    throw new IllegalStateException("This should never be called! The reason this method exists to make Enumerations work as functions.")

  //type Files = Traversable[AbstractFile] // TODO: move to package object in common together with other common types like AbstractFile, SUCCESS

  type Filters = Val with Function2[Regex, Files, Files]

  val FILTER = new Val with Function2[Regex, Files, Files] {
    override val toString = "FILTER"

    override def apply(regex: Regex, files: Files): Files =
      files filter { f => (regex findFirstIn f.filename) isDefined}
  }

  val FILTER_NOT = new Val with Function2[Regex, Files, Files] {
    override val toString = "FILTER_NOT"

    override def apply(regex: Regex, files: Files): Files =
      files filterNot { f => (regex findFirstIn f.filename) isDefined}
  }
}

// TODO: delete this later when tested with MongoDB
object DelMe extends App {
  //import ca.pgx.common.processors.Filters.Filter

  implicit val filez: Traversable[AbstractFile] = AbstractFile("somename.sql", 1024, DateTime.now.toDate) :: Nil

  println(Filters.FILTER("".r, filez))

  println(Filters.values)

  println(Filters.FILTER)
  println(Filters.FILTER_NOT)
  println(Filters.maxId)
  println(Filters.FILTER.id)
  println(Filters.FILTER_NOT.id)
  println(Filters.withName("FILTER"))



  object A extends Enumeration with Function1[Long, String] {
    override def apply(smth: Long): String = ??? // smth.toString

    type A = Value with Function1[Long, String]

    val ONE = new Val with Function1[Long, String] {
      override def toString = "ONE"
      override def apply(smth: Long): String = smth.toString + toString
    }

    val TWO = new Val with Function1[Long, String] {
      override def toString = "TWO"
      override def apply(smth: Long): String = smth.toString + toString
    }
  }

  import A._

  println(ONE, TWO)

  println(ONE(1L))
  println(TWO(2L))
  println(A(1L))

}
