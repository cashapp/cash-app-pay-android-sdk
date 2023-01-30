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

/**
 * A [RetryManager] implementation with max number of retries and back-off strategy.
 */
internal class RetryManagerImpl(private val maxRetries: Int = 4) : RetryManager {

  private var durationTillNextRetry = 1.5.toDuration(DurationUnit.SECONDS)
  private var retryCount = 0

  override fun shouldRetry(): Boolean {
    return retryCount <= maxRetries
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