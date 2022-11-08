/*
 * Copyright (C) 2023 Cash App
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package app.cash.paykit.analytics.core

import app.cash.paykit.analytics.AnalyticsLogger
import app.cash.paykit.analytics.persistence.AnalyticEntry
import app.cash.paykit.analytics.persistence.EntriesDataSource
import app.cash.paykit.analytics.persistence.toCommaSeparatedListIds

abstract class DeliveryHandler {
  abstract val deliverableType: String

  var logger: AnalyticsLogger? = null

  val deliveryListener: DeliveryListener
    get() = listener

  private var dataSource: EntriesDataSource? = null

  private val listener = object : DeliveryListener {
    override fun onSuccess(entries: List<AnalyticEntry>) {
      logger?.d(
        TAG,
        "successful delivery, deleting $deliverableType[" + entries.toCommaSeparatedListIds() + "]",
      )
      dataSource?.deleteEntry(entries)
    }

    override fun onError(entries: List<AnalyticEntry>) {
      logger?.d(
        TAG,
        "DELIVERY_FAILED for $deliverableType[" + entries.toCommaSeparatedListIds() + "]",
      )
      dataSource?.updateStatuses(entries, AnalyticEntry.STATE_DELIVERY_FAILED)
    }
  }

  internal fun setDependencies(dataSource: EntriesDataSource, logger: AnalyticsLogger) {
    this.dataSource = dataSource
    this.logger = logger
  }

  abstract fun deliver(
    entries: List<AnalyticEntry>,
    deliveryListener: DeliveryListener,
  )

  companion object {
    private const val TAG = "DeliveryHandler"
  }
}
