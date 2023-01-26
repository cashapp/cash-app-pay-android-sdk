package app.cash.paykit.analytics.core

/**
 * Represents data that needs to be delivered
 */
interface Deliverable {
  /** A descriptive name of the deliverable. We will use this value to match the `Deliverable` with the appropriate `DeliveryHandler` implementation. */
  val type: String

  /** A String representing the content to be delivered. */
  val content: String

  /** Meta data that you want to associate with the deliverable (optional) */
  val metaData: String?
}
