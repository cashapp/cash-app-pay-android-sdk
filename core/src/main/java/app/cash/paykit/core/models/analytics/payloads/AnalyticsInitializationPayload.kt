package app.cash.paykit.core.models.analytics.payloads

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * This payload corresponds to the (mobile_cap_pk_initialization)[https://es-manager.stage.sqprod.co/schema-manager/catalogs/1339] Catalog.
 */
@JsonClass(generateAdapter = true)
class AnalyticsInitializationPayload(
  /*
  * Common fields.
  */
  @Json(name = "mobile_cap_pk_initialization_sdk_version")
  sdkVersion: String,

  @Json(name = "mobile_cap_pk_initialization_client_ua")
  clientUserAgent: String,

  @Json(name = "mobile_cap_pk_initialization_platform")
  requestPlatform: String,

  @Json(name = "mobile_cap_pk_initialization_client_id")
  clientId: String,

  ) : AnalyticsBasePayload(sdkVersion, clientUserAgent, requestPlatform, clientId) {

  companion object {
    const val CATALOG = "mobile_cap_pk_initialization"
  }
}
