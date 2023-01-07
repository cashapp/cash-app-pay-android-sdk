package com.squareup.cash.paykit

import android.content.Context
import com.squareup.cash.paykit.exceptions.PayKitIntegrationException
import com.squareup.cash.paykit.impl.CashAppPayKitImpl
import com.squareup.cash.paykit.models.response.CustomerResponseData
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

/**
 * Robolectric is used for the Lifecycle observer
 */
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
      payKitLifecycleListener = mockk(),
      useSandboxEnvironment = true,
    )
}
