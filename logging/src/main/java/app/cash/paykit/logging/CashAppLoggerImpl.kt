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
package app.cash.paykit.logging

import android.util.Log

class CashAppLoggerImpl : CashAppLogger {

  private val history = CashAppLoggerHistory()

  override fun logVerbose(tag: String, msg: String) {
    history.log(CashAppLogEntry(Log.VERBOSE, tag, msg))
    Log.v(tag, msg)
  }

  override fun logWarning(tag: String, msg: String) {
    history.log(CashAppLogEntry(Log.WARN, tag, msg))
    Log.w(tag, msg)
  }

  override fun logError(tag: String, msg: String, throwable: Throwable?) {
    history.log(CashAppLogEntry(Log.ERROR, tag, msg, throwable))
    Log.e(tag, msg, throwable)
  }

  override fun retrieveLogs(): List<CashAppLogEntry> {
    return history.retrieveLogs()
  }
}
