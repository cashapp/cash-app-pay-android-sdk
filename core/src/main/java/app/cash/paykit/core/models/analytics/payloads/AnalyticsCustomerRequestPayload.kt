/*
 * Copyright (C) 2023 Cash App
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package app.cash.paykit.core.models.analytics.payloads

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * This payload corresponds to the (mobile_cap_pk_customer_request)[https://es-manager.stage.sqprod.co/schema-manager/catalogs/1341] Catalog.
 */
@JsonClass(generateAdapter = true)
data class AnalyticsCustomerRequestPayload(

  /*
  * Common fields.
  */
  @Json(name = "mobile_cap_pk_customer_request_sdk_version")
  override val sdkVersion: String,

  @Json(name = "mobile_cap_pk_customer_request_client_ua")
  override val clientUserAgent: String,

  @Json(name = "mobile_cap_pk_customer_request_platform")
  override val requestPlatform: String,

  @Json(name = "mobile_cap_pk_customer_request_client_id")
  override val clientId: String,

  /*
  * Create Request.
  */

  // This represents the SDK State.
  @Json(name = "mobile_cap_pk_customer_request_action")
  val action: String? = null,

  // A string built from the Payment Actions when creating a Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_create_actions")
  val createActions: String? = null,

  // The channel when creating a Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_create_channel")
  val createChannel: String? = null,

  // The redirect URL when creating a Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_create_redirect_url")
  val createRedirectUrl: String? = null,

  // The reference ID when creating a Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_create_reference_id")
  val createReferenceId: String? = null,

  // A string built from the metadata when creating a Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_create_metadata")
  val createMetadata: String? = null,

  /*
  * Generic Event properties.
   */

  // The status of the Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_status")
  val status: String? = null,

  // The channel when creating a Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_channel")
  val requestChannel: String? = null,

  // The ID of the Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_request_id")
  val requestId: String? = null,

  // A string built from the Payment Actions of a Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_actions")
  val actions: String? = null,

  // The mobile URL in the Auth Flow Trigger of the Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_auth_mobile_url")
  val authMobileUrl: String? = null,

  // The redirect URL of the Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_redirect_url")
  val redirectUrl: String? = null,

  // The created at timestamp of the Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_created_at")
  val createdAt: Long? = null,

  // The updated at timestamp of the Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_updated_at")
  val updatedAt: Long? = null,

  // The id of the Origin of the Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_origin_id")
  val originId: String? = null,

  // The type of the Origin of the Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_origin_type")
  val originType: String? = null,

  // A string built from the Grants of a Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_grants")
  val requestGrants: String? = null,

  // The reference ID of the Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_reference_id")
  val referenceId: String? = null,

  // The name of the Requester Profile in the Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_requester_name")
  val requesterName: String? = null,

  // The ID of the Customer Profile in the Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_customer_id")
  val customerId: String? = null,

  // The Cashtag of the Customer Profile in the Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_customer_cashtag")
  val customerCashTag: String? = null,

  // A string built from the metadata in the Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_metadata")
  val metadata: String? = null,

  /*
   * Update Request fields.
   */

  // A string built from the Payment Actions when updating a Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_update_actions")
  val updateActions: String? = null,

  // The reference ID when updating a Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_update_reference_id")
  val updateReferenceId: String? = null,

  // The redirect URL when creating a Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_update_metadata")
  val updateMetadata: String? = null,

  // A string built from the approved Grants of a Customer Request.
  @Json(name = "mobile_cap_pk_customer_request_approved_grants")
  val approvedGrants: String? = null,

  /*
   * Errors.
   */

  // The error category.
  @Json(name = "mobile_cap_pk_customer_request_error_category")
  val errorCategory: String? = null,

  // The error code.
  @Json(name = "mobile_cap_pk_customer_request_error_code")
  val errorCode: String? = null,

  // The detail message of the error.
  @Json(name = "mobile_cap_pk_customer_request_error_detail")
  val errorDetail: String? = null,

  // The field of the error.
  @Json(name = "mobile_cap_pk_customer_request_error_field")
  val errorField: String? = null,
) : AnalyticsBasePayload(sdkVersion, clientUserAgent, requestPlatform, clientId) {

  companion object {
    const val CATALOG = "mobile_cap_pk_customer_request"
  }
}
