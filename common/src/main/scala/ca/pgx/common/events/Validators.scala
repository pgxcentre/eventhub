package ca.pgx.common.events

import ca.pgx.common.processors.AbstractFile
import net.liftweb.common._
import com.github.nscala_time.time.Imports._
import scala.util.matching.Regex
import ca.pgx.common._

/**
 * Validations that can be applied to submitted data (files). These will
 * usually trigger onSuccess and onFailure actions.
 */
object Validators extends Enumeration with Function2[Long, Files, ValidationResult] {

  override def apply(x: Long, y: Files): ValidationResult =
    throw new IllegalStateException("This should never be called! The reason this method exists to make Enumerations work as functions.")

  // FIXME: generalize Long to T to allow other args? Accidentally it works now for all needs
  type Validators = Val with Function2[Long, Files, ValidationResult]

  val COUNT = new Val with Function2[Long, Files, ValidationResult] {
    override val toString = "COUNT"

    override def apply(expectedSize: Long, files: Files): ValidationResult =
      if(files.size == expectedSize) SUCCESS
      else Failure(s"Expected to find precisely $expectedSize entries, but found ${files.size}.")
  }

  val MIN_COUNT = new Val with Function2[Long, Files, ValidationResult] {
    override val toString = "MIN_COUNT"

    override def apply(expectedSize: Long, files: Files): ValidationResult =
      if(files.size < expectedSize) SUCCESS
      else Failure(s"Expected to find at least $expectedSize entries, but found ${files.size}.")
  }

  val MAX_COUNT = new Val with Function2[Long, Files, ValidationResult] {
    override val toString = "MAX_COUNT"

    override def apply(expectedSize: Long, files: Files): ValidationResult =
      if(files.size > expectedSize) SUCCESS
      else Failure(s"Expected to find at most $expectedSize entries, but found ${files.size}.")
  }

  /**
   * Max age of file in seconds. All files have to match the condition. To avoid failures apply filter
   * before calling this validation.
   */
  val MAX_AGE = new Val with Function2[Long, Files, ValidationResult] {
    override val toString = "MAX_AGE"

    override def apply(expectedAge: Long, files: Files): ValidationResult = {
      val secDur = Duration.standardSeconds(expectedAge)
      val borderLine = (DateTime.now - secDur).toDate
      if(files forall (_.dateModified after borderLine))
        SUCCESS
      else
        Failure(s"Found entries older than expected age: [$borderLine].")
    }
  }

  /**
   * Min age of file in seconds. All files have to match the condition. To avoid failures apply filter
   * before calling this validation.
   */
  val MIN_AGE = new Val with Function2[Long, Files, ValidationResult] {
    override val toString = "MIN_AGE"

    override def apply(expectedAge: Long, files: Files): ValidationResult = {
      val secDur = Duration.standardSeconds(expectedAge)
      val borderLine = (DateTime.now - secDur).toDate
      if(files forall (_.dateModified before borderLine))
        SUCCESS
      else
        Failure(s"Found entries younger than expected age: [$borderLine].")
    }
  }

  val SIZE = new Val with Function2[Long, Files, ValidationResult] {
    override val toString = "SIZE"

    override def apply(expectedSize: Long, files: Files): ValidationResult =
      if(files forall (_.sizeBytes == expectedSize)) SUCCESS
      else Failure(s"Found files that don't match exact size of $expectedSize.")
  }

  val MIN_SIZE = new Val with Function2[Long, Files, ValidationResult] {
    override val toString = "MIN_SIZE"

    override def apply(expectedSize: Long, files: Files): ValidationResult =
      if(files forall (_.sizeBytes >= expectedSize)) SUCCESS
      else Failure(s"Found files that are smaller than expected size $expectedSize.")
  }

  val MAX_SIZE = new Val with Function2[Long, Files, ValidationResult] {
    override val toString = "MAX_SIZE"

    override def apply(expectedSize: Long, files: Files): ValidationResult =
      if(files forall (_.sizeBytes <= expectedSize)) SUCCESS
      else Failure(s"Found files that are larger than expected size $expectedSize.")
  }

}
