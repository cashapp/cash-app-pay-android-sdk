package app.cash.paykit.analytics.core

import app.cash.paykit.analytics.AnalyticsLogger
import app.cash.paykit.analytics.persistence.AnalyticEntry
import app.cash.paykit.analytics.persistence.EntriesDataSource

internal abstract class DeliveryHandler {
  abstract val deliverableType: String

  var logger: AnalyticsLogger? = null

  val deliveryListener: DeliveryListener
    get() = listener

  private var dataSource: EntriesDataSource? = null

  private val listener = object : DeliveryListener {
    override fun onSuccess(entries: List<AnalyticEntry>) {
      logger?.d(
        TAG,
        "successful delivery, deleting $deliverableType[" +
          dataSource?.entryList2CommaSeparatedIds(entries) + "]",
      )
      dataSource?.deleteEntry(entries)
    }

    override fun onError(entries: List<AnalyticEntry>) {
      logger?.d(
        TAG,
        "DELIVERY_FAILED for $deliverableType[" +
          dataSource?.entryList2CommaSeparatedIds(entries) + "]",
      )
      dataSource?.updateStatuses(entries, AnalyticEntry.STATE_DELIVERY_FAILED)
    }
  }

  internal fun setDependencies(dataSource: EntriesDataSource, logger: AnalyticsLogger) {
    this.dataSource = dataSource
    this.logger = logger
  }

  internal abstract fun deliver(
    entries: List<AnalyticEntry>,
    deliveryListener: DeliveryListener? = null,
  )

  companion object {
    private const val TAG = "DeliveryHandler"
  }
}
