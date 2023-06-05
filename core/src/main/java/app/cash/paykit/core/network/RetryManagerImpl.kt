/*
 * Copyright (C) 2023 Cash App
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.paykit.core.network

import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * A [RetryManager] implementation with max number of retries and back-off strategy.
 */
@OptIn(ExperimentalTime::class)
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
