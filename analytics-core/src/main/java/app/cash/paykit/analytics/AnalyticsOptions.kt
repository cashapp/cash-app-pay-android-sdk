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
package app.cash.paykit.analytics

import android.util.Log
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class AnalyticsOptions constructor(

  /** Delay in seconds to wait until we begin to deliver events. */
  val delay: Duration = 0.seconds,

  /** Interval of time between uploading batches. */
  val interval: Duration = 30.seconds,

  /** Number of entries to include per process. */
  val maxEntryCountPerProcess: Int = 30,

  /** Number of events to include in a given batch process. */
  val batchSize: Int = 10,

  /** The name of the database file on disk. */
  val databaseName: String,

  /** The log level. */
  val logLevel: Int = Log.ERROR,

  /** whether or not logging is disabled */
  val isLoggerDisabled: Boolean = false,

  /** The version code of the application. */
  val applicationVersionCode: Int = 0,
) {
  init {
    if (!interval.isPositive()) {
      Log.e("PayKit", "Options interval must be > 0")
      if (BuildConfig.DEBUG) {
        throw IllegalArgumentException("Options interval must be > 0")
      }
    }
    if (delay.isNegative()) {
      Log.e("PayKit", "Options delay must be >= 0")
      if (BuildConfig.DEBUG) {
        throw IllegalArgumentException("Options interval must be > 0")
      }
    }
  }
}
