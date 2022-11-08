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

package app.cash.paykit.core.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit.MILLISECONDS

internal object OkHttpProvider {

  fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
      .connectTimeout(DEFAULT_NETWORK_TIMEOUT_MILLISECONDS, MILLISECONDS)
      .callTimeout(DEFAULT_NETWORK_TIMEOUT_MILLISECONDS, MILLISECONDS)
      .readTimeout(DEFAULT_NETWORK_TIMEOUT_MILLISECONDS, MILLISECONDS)
      .writeTimeout(DEFAULT_NETWORK_TIMEOUT_MILLISECONDS, MILLISECONDS)
      .build()
  }

  private const val DEFAULT_NETWORK_TIMEOUT_MILLISECONDS = 60_000L
}
