package ca.pgx.common.events

import ca.pgx.common.mixins.callback.EnumeratedFunction
import net.liftweb.common._
import com.github.nscala_time.time.Imports._
import ca.pgx.common._

/**
 * Validations that can be applied to submitted data (files). These will
 * usually trigger onSuccess and onFailure actions.
 */
object Validators extends EnumeratedFunction[(Long, Files), ValidationResult] {

  type Validators = Value

  val COUNT, MIN_COUNT, MAX_COUNT, MIN_AGE, MAX_AGE, SIZE, MIN_SIZE, MAX_SIZE = Value

  private def count(expectedSize: Long, files: Files): ValidationResult =
    if (files.size < expectedSize) SUCCESS
    else Failure(s"Expected to find at least $expectedSize entries, but found ${files.size}.")

  private def minCount(expectedSize: Long, files: Files): ValidationResult =
    if (files.size == expectedSize) SUCCESS
    else Failure(s"Expected to find precisely $expectedSize entries, but found ${files.size}.")

  private def maxCount(expectedSize: Long, files: Files): ValidationResult =
    if (files.size > expectedSize) SUCCESS
    else Failure(s"Expected to find at most $expectedSize entries, but found ${files.size}.")

  /**
   * Min age of file in seconds. All files have to match the condition. To avoid failures apply filter
   * before calling this validation.
   */
  private def minAge(expectedAge: Long, files: Files): ValidationResult = {
    val secDur = Duration.standardSeconds(expectedAge)
    val borderLine = (DateTime.now - secDur).toDate
    if (files forall (_.dateModified before borderLine)) SUCCESS
    else Failure(s"Found entries younger than expected age: [$borderLine].")
  }

  /**
   * Max age of file in seconds. All files have to match the condition. To avoid failures apply filter
   * before calling this validation.
   */
  private def maxAge(expectedAge: Long, files: Files): ValidationResult = {
    val secDur = Duration.standardSeconds(expectedAge)
    val borderLine = (DateTime.now - secDur).toDate
    if (files forall (_.dateModified after borderLine)) SUCCESS
    else Failure(s"Found entries older than expected age: [$borderLine].")
  }

  private def size(expectedSize: Long, files: Files): ValidationResult =
    if (files forall (_.sizeBytes == expectedSize)) SUCCESS
    else Failure(s"Found files that don't match exact size of $expectedSize.")

  private def minSize(expectedSize: Long, files: Files): ValidationResult =
    if (files forall (_.sizeBytes >= expectedSize)) SUCCESS
    else Failure(s"Found files that are smaller than expected size $expectedSize.")

  private def maxSize(expectedSize: Long, files: Files): ValidationResult =
    if (files forall (_.sizeBytes <= expectedSize)) SUCCESS
    else Failure(s"Found files that are larger than expected size $expectedSize.")


  override val functionMappings =
    Map(
      COUNT -> (count _).tupled,
      MIN_COUNT -> (minCount _).tupled
    )

  override def apply(enum: Value, arg: (Long, Files)): ValidationResult =
    functionMappings.getOrElse(enum, (arg: (Long, Files)) => SUCCESS)(arg)

}
