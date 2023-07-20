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

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class CashAppLoggerHistoryTests {
  private lateinit var loggerHistory: CashAppLoggerHistory

  @Before
  fun setUp() {
    loggerHistory = CashAppLoggerHistory()
  }

  @Test
  fun `test log adds entry to history`() {
    val entry = CashAppLogEntry(1, "tag1", "message1")
    loggerHistory.log(entry)
    assertThat(loggerHistory.retrieveLogs()).contains(entry)
  }

  @Test
  fun `test log removes first entry when history exceeds max size`() {
    val oldEntry = CashAppLogEntry(1, "tag1", "message1")
    val newEntry = CashAppLogEntry(2, "tag2", "message2")

    loggerHistory.log(oldEntry)
    repeat(200) {
      // this should remove the first "oldEntry" added above.
      loggerHistory.log(newEntry)
    }

    assertThat(loggerHistory.retrieveLogs()).doesNotContain(oldEntry)
    assertThat(loggerHistory.retrieveLogs()).contains(newEntry)
  }

  @Test
  fun `test retrieveLogs returns a list containing all entries in history`() {
    val entries = listOf(
      CashAppLogEntry(1, "tag1", "message1"),
      CashAppLogEntry(2, "tag2", "message2"),
      CashAppLogEntry(3, "tag3", "message3"),
    )

    entries.forEach {
      loggerHistory.log(it)
    }

    assertThat(loggerHistory.retrieveLogs()).containsExactlyElementsIn(entries)
  }
}
