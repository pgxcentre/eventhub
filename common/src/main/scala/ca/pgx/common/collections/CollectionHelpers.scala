package ca.pgx.common.collections

import ca.pgx.common._

/**
 * Various generic helper methods for collections.
 */
object CollectionHelpers {

  /**
   * Evaluates each receiver by applying transform on it with provided data. Returns first failure
   * encountered or success if no failures observed.
   * @param receivers
   * @param transform
   * @param data
   * @tparam T collection element type
   * @tparam U reference/validation data that is examined for each element of a collection by @transform
   * @return
   */
  def successOrFirstFailure[T, U](receivers: Traversable[T],
                                  transform: (T, U) => ValidationResult, data: U): ValidationResult =
    receivers match {
      case h :: t =>
        val result = transform(h, data)
        if (result.isDefined)
          successOrFirstFailure(t, transform, data)
        else
          result
      case _ => SUCCESS
    }
}
