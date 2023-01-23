package app.cash.paykit.analytics.core

import app.cash.paykit.analytics.AnalyticsLogger
import app.cash.paykit.analytics.AnalyticsOptions
import app.cash.paykit.analytics.persistence.AnalyticEntry
import app.cash.paykit.analytics.persistence.EntriesDataSource
import java.util.concurrent.Callable

internal class DeliveryWorker(
  private val dataSource: EntriesDataSource,
  private val handlers: List<DeliveryHandler> = emptyList(),
  private val logger: AnalyticsLogger = AnalyticsLogger(AnalyticsOptions()),
) : Callable<Void> {
  init {
    logger.d(TAG, "Worker initialized.")
  }

  @Throws(Exception::class)
  override fun call(): Void? {
    logger.d(TAG, "Starting delivery [$this]")
    for (deliveryHandler in handlers) {
      val entryType = deliveryHandler.deliverableType
      val processId: String = dataSource.generateProcessId(entryType)
      var entries: List<AnalyticEntry> =
        dataSource.getEntriesForDelivery(processId, entryType)
      if (entries.isNotEmpty()) {
        logger.d(
          TAG,
          String.format(
            "processing %s[%d] | processId=%s",
            entryType,
            entries.size,
            processId,
          ),
        )
      }
      while (entries.isNotEmpty()) {
        logger.d(
          TAG,
          "DELIVERY_IN_PROGRESS for ids[" + dataSource.entryList2CommaSeparatedIds(
            entries,
          ) + "]",
        )
        dataSource.updateStatuses(entries, AnalyticEntry.STATE_DELIVERY_IN_PROGRESS)
        deliveryHandler.deliver(entries, deliveryHandler.deliveryListener)

        // get the next batch of events to send
        entries = dataSource.getEntriesForDelivery(processId, entryType)
        if (entries.isNotEmpty()) {
          logger.d(
            TAG,
            String.format(
              "Processing %s[%d] | processId=%s",
              entryType,
              entries.size,
              processId,
            ),
          )
        }
      }
    }
    logger.d(TAG, "Delivery finished. [$this]")
    return null
  }

  companion object {
    private const val TAG = "DeliveryWorker"
  }
}
