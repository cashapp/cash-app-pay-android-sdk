package app.cash.paykit.analytics.core

import app.cash.paykit.analytics.AnalyticsLogger
import app.cash.paykit.analytics.persistence.AnalyticEntry
import app.cash.paykit.analytics.persistence.EntriesDataSource
import app.cash.paykit.analytics.persistence.toCommaSeparatedList

abstract class DeliveryHandler {
  abstract val deliverableType: String

  var logger: AnalyticsLogger? = null

  val deliveryListener: DeliveryListener
    get() = listener

  private var dataSource: EntriesDataSource? = null

  private val listener = object : DeliveryListener {
    override fun onSuccess(entries: List<AnalyticEntry>) {
      logger?.d(TAG, "successful delivery, deleting $deliverableType[" + entries.toCommaSeparatedList() + "]")
      dataSource?.deleteEntry(entries)
    }

    override fun onError(entries: List<AnalyticEntry>) {
      logger?.d(TAG, "DELIVERY_FAILED for $deliverableType[" + entries.toCommaSeparatedList() + "]")
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
