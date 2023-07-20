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

import java.util.LinkedList

internal class CashAppLoggerHistory {
  companion object {
    private const val HISTORY_MAX_SIZE = 200
  }

  private val history = LinkedList<CashAppLogEntry>()

  fun log(entry: CashAppLogEntry) {
    history.add(entry)
    if (history.size > HISTORY_MAX_SIZE) {
      history.removeFirst()
    }
  }

  fun retrieveLogs(): List<CashAppLogEntry> {
    return history.toList()
  }
}
