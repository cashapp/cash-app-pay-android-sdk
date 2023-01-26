package app.cash.paykit.core.models.analytics.payloads

open class AnalyticsBasePayload(
  // Version of the SDK.
  val sdkVersion: String,

  // User Agent of the app.
  val clientUserAgent: String,

  val requestPlatform: String,

  val clientId: String,
)
