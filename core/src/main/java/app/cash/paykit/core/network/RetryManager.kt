package app.cash.paykit.core.network

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal interface RetryManager {
  fun shouldRetry(): Boolean

  fun timeUntilNextRetry(): Duration

  fun networkAttemptFailed()

  fun getRetryCount(): Int
}

internal class RetryManagerOptions(
  val maxRetries: Int = 4,
  val initialDuration: Duration = 1.5.toDuration(DurationUnit.SECONDS),
)

/**
 * A [RetryManager] implementation with max number of retries and back-off strategy.
 */
internal class RetryManagerImpl(
  private val retryManagerOptions: RetryManagerOptions,
) : RetryManager {

  private var durationTillNextRetry = retryManagerOptions.initialDuration
  private var retryCount = 0

  override fun shouldRetry(): Boolean {
    return retryCount <= retryManagerOptions.maxRetries
  }

  override fun timeUntilNextRetry(): Duration {
    return durationTillNextRetry
  }

  override fun networkAttemptFailed() {
    retryCount++
    durationTillNextRetry = durationTillNextRetry.times(2)
  }

  override fun getRetryCount(): Int = retryCount
}
