package app.cash.paykit.core.models.common

/**
 * This class wraps all I/O-related requests in one of 2 states: [Success] or [Failure].
 */
internal sealed interface NetworkResult<in T> {

  class Failure<T>(val exception: Exception) : NetworkResult<T>

  class Success<T>(val data: T) : NetworkResult<T>

  companion object {
    fun <T> success(data: T): NetworkResult<T> = Success(data)

    fun <T> failure(exception: Exception): NetworkResult<T> =
      Failure(exception)
  }
}
