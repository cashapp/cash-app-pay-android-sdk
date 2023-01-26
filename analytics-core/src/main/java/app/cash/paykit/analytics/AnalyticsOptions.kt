package app.cash.paykit.analytics

import android.util.Log
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class AnalyticsOptions(
  /** Delay in seconds to wait until we begin to deliver events. */
  val delay: Duration = 0.seconds,

  /** Interval of time between uploading batches. */
  val interval: Duration = 30.seconds,

  /** Number of entries to include per process. */
  val maxEntryCountPerProcess: Int = 30,

  /** Number of events to include in a given batch process. */
  val batchSize: Int = 10,

  /** The name of the database file on disk. */
  val databaseName: String = "analytic-entries.db",

  /** The log level. */
  val logLevel: Int = Log.ERROR,

  /** whether or not logging is disabled */
  val isLoggerDisabled: Boolean = false,

  /** The version code of the application. */
  val applicationVersionCode: Int = 0,
) {

  internal constructor(builder: Builder) : this(
    builder.delay,
    builder.interval,
    builder.maxEntryCountPerProcess,
    builder.batchSize,
    builder.databaseName,
    builder.logLevel,
    builder.isLoggerDisabled,
    builder.applicationVersionCode,
  )

  companion object {
    inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
  }

  class Builder {
    var delay: Duration = 0.seconds
    var interval: Duration = 30.seconds
    var maxEntryCountPerProcess: Int = 30
    var batchSize: Int = 10
    var databaseName: String = "analytic-events.db"
    var logLevel: Int = Log.ERROR
    var isLoggerDisabled: Boolean = false
    var applicationVersionCode: Int = 0

    fun build(): AnalyticsOptions {
      check(interval.isPositive()) { "interval must be > 0" }
      check(!delay.isNegative()) { "delay must be >= 0" }
      return AnalyticsOptions(this)
    }
  }
}
