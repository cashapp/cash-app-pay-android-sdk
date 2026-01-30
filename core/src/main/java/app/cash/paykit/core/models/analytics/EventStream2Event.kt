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
package app.cash.paykit.core.models.analytics

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class EventStream2Event(
  @Json(name = "app_name")
  val appName: String,
  @Json(name = "catalog_name")
  val catalogName: String,
  @Json(name = "json_data")
  val jsonData: String,
  @Json(name = "recorded_at_usec")
  val recordedAt: Long,
  @Json(name = "uuid")
  val uuid: String,
)
