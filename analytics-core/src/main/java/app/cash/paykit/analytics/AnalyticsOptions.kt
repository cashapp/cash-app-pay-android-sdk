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
  val databaseName: String = "paykit-events.db",

  /** The log level. */
  val logLevel: Int = Log.ERROR,

  /** whether or not logging is disabled */
  val isLoggerDisabled: Boolean = false,

  /** The version code of the application. */
  val applicationVersionCode: Int = 0,
) {
  init {
    check(interval.isPositive()) { "Options interval must be > 0" }
    check(!delay.isNegative()) { "Options delay must be >= 0" }
  }
}
