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
package app.cash.paykit.core.analytics

import app.cash.paykit.analytics.core.Deliverable

/**
 * Class that represents the payload to be delivered to the ES2 API.
 */
internal data class AnalyticsEventStream2Event constructor(
  override val content: String,
) : Deliverable {
  override val type = ES_EVENT_TYPE
  override val metaData = null

  companion object {
    const val ES_EVENT_TYPE = "AnalyticsEventStream2Event"
  }
}
