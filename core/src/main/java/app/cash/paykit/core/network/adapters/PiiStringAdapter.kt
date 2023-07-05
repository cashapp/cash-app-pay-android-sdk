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
package app.cash.paykit.core.network.adapters

import app.cash.paykit.core.models.pii.PiiString
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token.NULL
import com.squareup.moshi.JsonWriter

internal class PiiStringAdapter : JsonAdapter<PiiString>() {

  override fun fromJson(reader: JsonReader): PiiString? {
    if (reader.peek() == NULL) {
      return reader.nextNull<PiiString>()
    }
    val plainString = reader.nextString()
    return PiiString(plainString)
  }

  override fun toJson(writer: JsonWriter, value: PiiString?) {
    if (value == null) {
      writer.nullValue()
    } else {
      writer.value(value.getRedacted())
    }
  }
}
