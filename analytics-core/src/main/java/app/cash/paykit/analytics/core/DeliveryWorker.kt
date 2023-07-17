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
package app.cash.paykit.analytics.core

import app.cash.paykit.analytics.AnalyticsLogger
import app.cash.paykit.analytics.persistence.AnalyticEntry
import app.cash.paykit.analytics.persistence.EntriesDataSource
import app.cash.paykit.analytics.persistence.toCommaSeparatedListIds
import java.util.*
import java.util.concurrent.Callable

internal class DeliveryWorker(
  private val dataSource: EntriesDataSource,
  private val handlers: List<DeliveryHandler> = emptyList(),
  private val logger: AnalyticsLogger,
) : Callable<Unit> {
  init {
    logger.v(TAG, "DeliveryWorker initialized.")
  }

  @Throws(Exception::class)
  override fun call() {
    logger.v(TAG, "Starting delivery [$this]")
    for (deliveryHandler in handlers) {
      val entryType = deliveryHandler.deliverableType
      val processId: String = dataSource.generateProcessId(entryType)
      var entries: List<AnalyticEntry> =
        dataSource.getEntriesForDelivery(processId, entryType)
      if (entries.isNotEmpty()) {
        logger.v(
          TAG,
          "Processing %s[%d] | processId=%s".format(Locale.US, entries, entries.size, processId),
        )
      }
      while (entries.isNotEmpty()) {
        logger.v(TAG, "DELIVERY_IN_PROGRESS for ids[" + entries.toCommaSeparatedListIds() + "]")
        dataSource.updateStatuses(entries, AnalyticEntry.STATE_DELIVERY_IN_PROGRESS)
        deliveryHandler.deliver(entries, deliveryHandler.deliveryListener)

        // get the next batch of events to send
        entries = dataSource.getEntriesForDelivery(processId, entryType)
        if (entries.isNotEmpty()) {
          logger.v(
            TAG,
            "Processing %s[%d] | processId=%s".format(Locale.US, entries, entries.size, processId),
          )
        }
      }
    }
    logger.v(TAG, "Delivery finished. [$this]")
  }

  companion object {
    private const val TAG = "DeliveryWorker"
  }
}
