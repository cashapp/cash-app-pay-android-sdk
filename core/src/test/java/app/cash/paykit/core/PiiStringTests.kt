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
package app.cash.paykit.core

import app.cash.paykit.core.models.pii.PiiString
import app.cash.paykit.core.network.adapters.InstantAdapter
import app.cash.paykit.core.network.adapters.PiiStringClearTextAdapter
import app.cash.paykit.core.network.adapters.PiiStringRedactAdapter
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import kotlinx.datetime.Instant
import org.junit.Test

class PiiStringTests {

  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun `test Redact Adapter will redact contents of PiiString`() {
    val piiString = PiiString("1234567890")
    val moshi = Moshi.Builder()
      .add(Instant::class.java, InstantAdapter())
      .add(PiiString::class.java, PiiStringRedactAdapter())
      .build()

    val serialized: JsonAdapter<PiiString> = moshi.adapter()
    assertThat(serialized.toJson(piiString)).isEqualTo("\"FILTERED\"")
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun `test Clear Text PiiString Adapter will NOT redact contents of PiiString`() {
    val piiString = PiiString("1234567890")
    val moshi = Moshi.Builder()
      .add(Instant::class.java, InstantAdapter())
      .add(PiiString::class.java, PiiStringClearTextAdapter())
      .build()

    val serialized: JsonAdapter<PiiString> = moshi.adapter()
    assertThat(serialized.toJson(piiString)).isEqualTo("\"$piiString\"")
  }

  @Test
  fun `test PiiString can be obtained as plain text`() {
    val value = "1234567890"
    val piiString = PiiString(value)
    assertThat(piiString.toString()).isEqualTo(value)
  }
}
