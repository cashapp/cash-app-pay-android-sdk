package app.cash.paykit.analytics

import android.util.Log

class AnalyticsLogger(
  private val options: AnalyticsOptions = AnalyticsOptions(),
) {
  fun v(tag: String, msg: String) {
    if (options.logLevel <= Log.VERBOSE) {
      log(Log.VERBOSE, tag, msg)
    }
  }

  fun d(tag: String, msg: String) {
    if (options.logLevel <= Log.DEBUG) {
      log(Log.DEBUG, tag, msg)
    }
  }

  fun i(tag: String, msg: String) {
    if (options.logLevel <= Log.INFO) {
      log(Log.INFO, tag, msg)
    }
  }

  fun w(tag: String, msg: String) {
    if (options.logLevel <= Log.WARN) {
      log(Log.WARN, tag, msg)
    }
  }

  fun e(tag: String, msg: String) {
    if (options.logLevel <= Log.ERROR) {
      log(Log.ERROR, tag, msg)
    }
  }

  private fun log(priority: Int, tag: String, msg: String) {
    if (!options.isLoggerDisabled) {
      Log.println(priority, tag, msg)
    }
  }
}
