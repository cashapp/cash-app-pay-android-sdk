package app.cash.paykit.analytics.core

import app.cash.paykit.analytics.persistence.AnalyticEntry

interface DeliveryListener {
  fun onSuccess(entries: List<AnalyticEntry>)
  fun onError(entries: List<AnalyticEntry>)
}
