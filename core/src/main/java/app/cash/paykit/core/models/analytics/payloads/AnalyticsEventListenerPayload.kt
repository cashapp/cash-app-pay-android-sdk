package app.cash.paykit.core.models.analytics.payloads

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * This payload corresponds to the (mobile_cap_pk_event_listener)[https://es-manager.stage.sqprod.co/schema-manager/catalogs/1340] Catalog.
 */
@JsonClass(generateAdapter = true)
class AnalyticsEventListenerPayload(
  /*
  * Common fields.
  */
  @Json(name = "mobile_cap_pk_event_listener_sdk_version")
  sdkVersion: String,

  @Json(name = "mobile_cap_pk_event_listener_client_ua")
  clientUserAgent: String,

  @Json(name = "mobile_cap_pk_event_listener_platform")
  requestPlatform: String,

  @Json(name = "mobile_cap_pk_event_listener_client_id")
  clientId: String,

  /*
  * Event Specific fields.
   */

  /**
   * True if the listener is being added.
   */
  @Json(name = "mobile_cap_pk_event_listener_is_added")
  val isAdded: Boolean,

) : AnalyticsBasePayload(sdkVersion, clientUserAgent, requestPlatform, clientId) {

  companion object {
    const val CATALOG = "mobile_cap_pk_event_listener"
  }
}
