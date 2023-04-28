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
import app.cash.paykit.core.models.response.GrantType.UNKNOWN
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Grant(
  @Json(name = "id")
  val id: String,
  @Json(name = "status")
  val status: String,
  @Json(name = "type")
  val type: GrantType = UNKNOWN,
  @Json(name = "action")
  val action: Action,
  @Json(name = "channel")
  val channel: String,
  @Json(name = "customer_id")
  val customerId: String,
  @Json(name = "updated_at")
  val updatedAt: String,
  @Json(name = "created_at")
  val createdAt: String,
  @Json(name = "expires_at")
  val expiresAt: String,
)
