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
package app.cash.paykit.core.models.response

import app.cash.paykit.core.models.common.Action
import app.cash.paykit.core.models.pii.PiiString
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.datetime.Clock.System
import kotlinx.datetime.Instant

const val STATUS_PENDING = "PENDING"
const val STATUS_PROCESSING = "PROCESSING"
const val STATUS_APPROVED = "APPROVED"
const val STATUS_DECLINED = "DECLINED"

@JsonClass(generateAdapter = true)
data class CustomerResponseData(
  @Json(name = "actions")
  val actions: List<Action>,
  @Json(name = "auth_flow_triggers")
  val authFlowTriggers: AuthFlowTriggers?,
  @Json(name = "channel")
  val channel: String,
  @Json(name = "id")
  val id: String,
  @Json(name = "origin")
  val origin: Origin,
  @Json(name = "requester_profile")
  val requesterProfile: RequesterProfile?,
  @Json(name = "status")
  val status: String,
  @Json(name = "updated_at")
  val updatedAt: Instant,
  @Json(name = "created_at")
  val createdAt: Instant,
  @Json(name = "expires_at")
  val expiresAt: Instant,
  @Json(name = "customer_profile")
  val customerProfile: CustomerProfile?,
  @Json(name = "grants")
  val grants: List<Grant>?,
  @Json(name = "reference_id")
  val referenceId: PiiString?,
) {
  fun isAuthTokenExpired(): Boolean {
    val now = System.now()
    val isExpired = now > (authFlowTriggers?.refreshesAt ?: return false)
    return isExpired
  }
}
