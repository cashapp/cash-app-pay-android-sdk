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

package app.cash.paykit.core

import app.cash.paykit.core.utils.UserAgentProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class UserAgentProviderTests {

  @Test
  fun testUserAgentFormat() {
    val userAgent = UserAgentProvider.provideUserAgent(RuntimeEnvironment.getApplication())
    // Example of what Robolectric env. will produce: "app.cash.paykit.core.test (Android 12; robolectric; robolectric; robolectric; en_US) PayKitVersion/0.0.6-SNAPSHOT".
    assertThat(userAgent).contains("app.cash.paykit.core.test")
    assertThat(userAgent).containsMatch("Android ..")
    assertThat(userAgent).containsMatch("PayKitVersion/.")
    assertThat(userAgent).contains("robolectric")
    assertThat(userAgent).contains("en_US")
  }
}
