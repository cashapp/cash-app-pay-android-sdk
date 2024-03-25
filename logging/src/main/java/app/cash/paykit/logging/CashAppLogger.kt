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

interface CashAppLogger {

  /**
   * Log a message with level VERBOSE.
   */
  fun logVerbose(tag: String, msg: String)

  /**
   * Log a message with level WARNING.
   */
  fun logWarning(tag: String, msg: String)

  /**
   * Log a message with level ERROR. Optionally include a [Throwable] to log along the error message.
   */
  fun logError(tag: String, msg: String, throwable: Throwable? = null)

  /**
   * Retrieve all logs.
   */
  fun retrieveLogs(): List<CashAppLogEntry>

  /**
   * Retrieves all logs, compiled as a single string.
   * Each log entry is separated by a couple of newline characters.
   * The format of each log entry is: "LEVEL: MESSAGE".
   *
   * If you need more control over the format, use [retrieveLogs] instead.
   */
  fun logsAsString(): String

  /**
   * Set a listener to be notified when a new log is added.
   */
  fun setListener(listener: CashAppLoggerListener)

  /**
   * Remove the currently registered listener, if any.
   */
  fun removeListener()
}

interface CashAppLoggerListener {
  fun onNewLog(log: CashAppLogEntry)
}
