package app.cash.paykit.core.models.analytics.payloads

open class AnalyticsBasePayload(
  // Version of the SDK.
  open val sdkVersion: String,

  // User Agent of the app.
  open val clientUserAgent: String,

  open val requestPlatform: String,

  open val clientId: String,
)
