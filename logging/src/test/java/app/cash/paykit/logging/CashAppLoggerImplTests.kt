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
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [ShadowLog::class])
class CashAppLoggerImplTests {

  private lateinit var logger: CashAppLoggerImpl
  private lateinit var fakeListener: CashAppLoggerListener
  private lateinit var logEntries: MutableList<CashAppLogEntry>

  @Before
  fun setUp() {
    ShadowLog.stream = System.out
    logger = CashAppLoggerImpl()
    logEntries = mutableListOf()

    fakeListener = object : CashAppLoggerListener {
      override fun onNewLog(log: CashAppLogEntry) {
        logEntries.add(log)
      }
    }
    logger.setListener(fakeListener)
  }

  @Test
  fun `test logVerbose logs correctly`() {
    val tag = "tag"
    val msg = "verbose log"

    logger.logVerbose(tag, msg)

    val expectedLogEntry = CashAppLogEntry(Log.VERBOSE, tag, msg)
    assertThat(logger.retrieveLogs()).contains(expectedLogEntry)
    assertThat(logEntries).contains(expectedLogEntry)
    assertThat(ShadowLog.getLogs().any { it.tag == tag && it.msg == msg && it.type == Log.VERBOSE }).isTrue()
  }

  @Test
  fun `test logWarning logs correctly`() {
    val tag = "tag"
    val msg = "warning log"

    logger.logWarning(tag, msg)

    val expectedLogEntry = CashAppLogEntry(Log.WARN, tag, msg)
    assertThat(logger.retrieveLogs()).contains(expectedLogEntry)
    assertThat(logEntries).contains(expectedLogEntry)
    assertThat(ShadowLog.getLogs().any { it.tag == tag && it.msg == msg && it.type == Log.WARN }).isTrue()
  }

  @Test
  fun `test logError logs correctly`() {
    val tag = "tag"
    val msg = "error log"
    val throwable = Throwable("stuff happens")

    logger.logError(tag, msg, throwable)

    val expectedLogEntry = CashAppLogEntry(Log.ERROR, tag, msg, throwable)
    assertThat(logger.retrieveLogs()).contains(expectedLogEntry)
    assertThat(logEntries).contains(expectedLogEntry)
    assertThat(ShadowLog.getLogs().any { it.tag == tag && it.msg == msg && it.throwable == throwable && it.type == Log.ERROR }).isTrue()
  }

  @Test
  fun `test removeListener removes the listener`() {
    logger.removeListener()
    logger.logVerbose("tag", "message")

    assertThat(logEntries).isEmpty()
  }
}
