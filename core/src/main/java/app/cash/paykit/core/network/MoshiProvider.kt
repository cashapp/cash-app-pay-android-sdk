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
package app.cash.paykit.core.network

import app.cash.paykit.core.models.pii.PiiString
import app.cash.paykit.core.network.adapters.InstantAdapter
import app.cash.paykit.core.network.adapters.PiiStringClearTextAdapter
import app.cash.paykit.core.network.adapters.PiiStringRedactAdapter
import com.squareup.moshi.Moshi
import kotlinx.datetime.Instant

internal object MoshiProvider {
  fun provideDefault(redactPii: Boolean = false): Moshi {
    val builder = Moshi.Builder()
      .add(Instant::class.java, InstantAdapter())

    if (redactPii) {
      builder.add(PiiString::class.java, PiiStringRedactAdapter())
    } else {
      builder.add(PiiString::class.java, PiiStringClearTextAdapter())
    }

    return builder.build()
  }
}
