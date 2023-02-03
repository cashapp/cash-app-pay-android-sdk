package app.cash.paykit.core.analytics

import app.cash.paykit.analytics.core.Deliverable

/**
 * Class that represents the payload to be delivered to the ES2 API.
 */
internal data class EventStream2Event constructor(
  override val content: String,
) : Deliverable {
  override val type = ESEventType
  override val metaData = null

  companion object {
    const val ESEventType = "EventStream2Event"
  }
}
