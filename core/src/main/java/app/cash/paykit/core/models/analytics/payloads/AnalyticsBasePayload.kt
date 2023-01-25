package app.cash.paykit.core.models.analytics.payloads

import com.squareup.moshi.Json

open class AnalyticsBasePayload(
  // Version of the SDK.
  @Json(name = "mobile_cap_pk_customer_request_sdk_version")
  val sdkVersion: String,

  // User Agent of the app.
  @Json(name = "mobile_cap_pk_customer_request_client_ua")
  val clientUserAgent: String,

  @Json(name = "mobile_cap_pk_customer_platform")
  val requestPlatform: String,

  @Json(name = "mobile_cap_pk_customer_client_id")
  val clientId: String,
)
