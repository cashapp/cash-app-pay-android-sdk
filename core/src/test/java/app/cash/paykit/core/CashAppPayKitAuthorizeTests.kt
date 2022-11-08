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

import android.content.Context
import app.cash.paykit.core.exceptions.PayKitIntegrationException
import app.cash.paykit.core.impl.CashAppPayKitImpl
import app.cash.paykit.core.models.response.CustomerResponseData
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class CashAppPayKitAuthorizeTests {

  @MockK(relaxed = true)
  private lateinit var context: Context

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test(expected = PayKitIntegrationException::class)
  fun `should throw if calling authorize before createCustomer`() {
    val payKit = createPayKit()
    payKit.registerForStateUpdates(mockk())
    payKit.authorizeCustomerRequest(context)
  }

  @Test(expected = PayKitIntegrationException::class)
  fun `should throw on authorizeCustomerRequest if has NOT registered for state updates`() {
    val payKit = createPayKit()
    val customerResponseData = mockk<CustomerResponseData>(relaxed = true)
    payKit.authorizeCustomerRequest(context, customerResponseData)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `should throw if missing mobileUrl from customer data`() {
    val payKit = createPayKit()
    val customerResponseData = mockk<CustomerResponseData>(relaxed = true)
    payKit.registerForStateUpdates(mockk())

    payKit.authorizeCustomerRequest(context, customerResponseData)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `should throw if unable to parse mobile url in customer data`() {
    val payKit = createPayKit()
    val customerResponseData = mockk<CustomerResponseData>(relaxed = true) {
      every { authFlowTriggers } returns null
    }
    payKit.registerForStateUpdates(mockk())

    payKit.authorizeCustomerRequest(context, customerResponseData)
  }

  @Test(expected = RuntimeException::class)
  fun `should throw on if unable to start mobileUrl activity`() {
    val payKit = createPayKit()
    val customerResponseData = mockk<CustomerResponseData>(relaxed = true) {
      every { authFlowTriggers } returns mockk {
        every { mobileUrl } returns "http://url"
      }
    }
    payKit.registerForStateUpdates(mockk())

    payKit.authorizeCustomerRequest(context, customerResponseData)
  }

  private fun createPayKit() =
    CashAppPayKitImpl(
      clientId = FakeData.CLIENT_ID,
      networkManager = mockk(),
      payKitLifecycleListener = mockk(relaxed = true),
      useSandboxEnvironment = true,
      analyticsEventDispatcher = mockk(relaxed = true),
    )
}
