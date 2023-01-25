package app.cash.paykit.core.models.analytics.payloads

import com.squareup.moshi.JsonClass

/**
 * This payload corresponds to the (mobile_cap_pk_initialization)[https://es-manager.stage.sqprod.co/schema-manager/catalogs/1339] Catalog.
 */
@JsonClass(generateAdapter = true)
class AnalyticsInitializationPayload(

  /*
  * Common fields.
  */

  sdkVersion: String,
  clientUserAgent: String,
  requestPlatform: String,
  clientId: String,

) : AnalyticsBasePayload(sdkVersion, clientUserAgent, requestPlatform, clientId) {

  companion object {
    const val CATALOG = "mobile_cap_pk_initialization"
  }
}
