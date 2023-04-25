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
package app.cash.paykit.core.models.common

import com.squareup.moshi.Json

data class Action(
  @Json(name = "amount")
  val amount_cents: Int? = null,
  @Json(name = "currency")
  val currency: String? = null,
  @Json(name = "scope_id")
  val scopeId: String,
  @Json(name = "type")
  val type: String,
)
