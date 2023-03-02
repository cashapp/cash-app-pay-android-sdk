/*
 * Copyright (C) 2023 Cash App
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

  @Json(name = "mobile_cap_pk_event_listener_is_sandbox")
  override val isSandbox: Boolean,

  /*
  * Event Specific fields.
   */

  /**
   * True if the listener is being added.
   */
  @Json(name = "mobile_cap_pk_event_listener_is_added")
  val isAdded: Boolean,

) : AnalyticsBasePayload(sdkVersion, clientUserAgent, requestPlatform, clientId, isSandbox) {

  companion object {
    const val CATALOG = "mobile_cap_pk_event_listener"
  }
}
