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
package app.cash.paykit.analytics.persistence

import android.database.Cursor

/**
 * Object model of the sync_entries database table.
 */
data class AnalyticEntry constructor(
  val id: Long = 0,
  val type: String? = null,
  val content: String,
  val state: Int = 0,
  val metaData: String? = null,
  val processId: String? = null,
  val version: String? = null,
) {

  companion object {
    const val STATE_NEW = 0
    const val STATE_DELIVERY_PENDING = 1
    const val STATE_DELIVERY_IN_PROGRESS = 2
    const val STATE_DELIVERY_FAILED = 3

    fun from(cursor: Cursor) = AnalyticEntry(
      id = cursor.getLong(0),
      type = cursor.getString(1),
      content = cursor.getString(2),
      state = cursor.getInt(3),
      metaData = cursor.getString(4),
      processId = cursor.getString(5),
      version = cursor.getString(6),
    )
  }
}
