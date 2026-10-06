/*
 * Copyright (C) 2026 Cash App
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

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class CashAppPayFactoryBaseUrlTests {

  @Test
  fun `customerRequestBaseUrl appends the customer request path`() {
    assertThat(CashAppPayFactory.customerRequestBaseUrl("https://example.com"))
      .isEqualTo("https://example.com/customer-request/v1/")
  }

  @Test
  fun `customerRequestBaseUrl keeps the production and sandbox endpoints unchanged`() {
    assertThat(CashAppPayFactory.customerRequestBaseUrl("https://api.cash.app"))
      .isEqualTo("https://api.cash.app/customer-request/v1/")
    assertThat(CashAppPayFactory.customerRequestBaseUrl("https://sandbox.api.cash.app"))
      .isEqualTo("https://sandbox.api.cash.app/customer-request/v1/")
  }

  @Test
  fun `customerRequestBaseUrl accepts a trailing slash`() {
    assertThat(CashAppPayFactory.customerRequestBaseUrl("https://example.com/"))
      .isEqualTo("https://example.com/customer-request/v1/")
  }

  @Test
  fun `customerRequestBaseUrl rejects a URL with a path`() {
    assertThrows(IllegalArgumentException::class.java) {
      CashAppPayFactory.customerRequestBaseUrl("https://example.com/customer-request/v1/")
    }
  }

  @Test
  fun `customerRequestBaseUrl rejects an invalid URL`() {
    assertThrows(IllegalArgumentException::class.java) {
      CashAppPayFactory.customerRequestBaseUrl("example.com")
    }
  }
}
