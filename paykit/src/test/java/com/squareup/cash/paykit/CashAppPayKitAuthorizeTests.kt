package com.squareup.cash.paykit

import android.content.Context
import com.squareup.cash.paykit.PayKitState.Authorizing
import com.squareup.cash.paykit.exceptions.PayKitIntegrationException
import com.squareup.cash.paykit.models.response.CustomerResponseData
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Robolectric is used for the Lifecycle observer
 */
@RunWith(RobolectricTestRunner::class)
class CashAppPayKitAuthorizeTests {

  @MockK(relaxed = true)
  private lateinit var context: Context

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test(expected = PayKitIntegrationException::class)
  fun `should throw if calling authorize before createCustomer`() {
    val payKit = CashAppPayKit(FakeData.CLIENT_ID, useSandboxEnvironment = true)
    payKit.registerForStateUpdates(mockk())
    payKit.authorizeCustomerRequest(context)
  }

  @Test(expected = PayKitIntegrationException::class)
  fun `should throw on authorizeCustomerRequest if has NOT registered for state updates`() {
    val payKit = CashAppPayKit(FakeData.CLIENT_ID, useSandboxEnvironment = true)
    val customerResponseData = mockk<CustomerResponseData>(relaxed = true)
    payKit.authorizeCustomerRequest(context, customerResponseData)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `should throw if missing mobileUrl from customer data`() {
    val payKit = CashAppPayKit(FakeData.CLIENT_ID, useSandboxEnvironment = true)
    val customerResponseData = mockk<CustomerResponseData>(relaxed = true)
    payKit.registerForStateUpdates(mockk())

    payKit.authorizeCustomerRequest(context, customerResponseData)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `should throw if unable to parse mobile url in customer data`() {
    val payKit = CashAppPayKit(FakeData.CLIENT_ID, useSandboxEnvironment = true)
    val customerResponseData = mockk<CustomerResponseData>(relaxed = true) {
      every { authFlowTriggers } returns null
    }
    payKit.registerForStateUpdates(mockk())

    payKit.authorizeCustomerRequest(context, customerResponseData)
  }

  @Test(expected = RuntimeException::class)
  fun `should throw on if unable to start mobileUrl activity`() {
    val payKit = CashAppPayKit(FakeData.CLIENT_ID, useSandboxEnvironment = true)
    val customerResponseData = mockk<CustomerResponseData>(relaxed = true) {
      every { authFlowTriggers } returns mockk {
        every { mobileUrl } returns "http://url"
      }
    }
    payKit.registerForStateUpdates(mockk())

    payKit.authorizeCustomerRequest(context, customerResponseData)
  }

  @Test
  fun `happy path`() {
    val payKit = CashAppPayKit(FakeData.CLIENT_ID, useSandboxEnvironment = true)
    val customerResponseData = mockk<CustomerResponseData>(relaxed = true) {
      every { authFlowTriggers } returns mockk {
        every { mobileUrl } returns "http://url"
      }
    }
    val listener = mockk<CashAppPayKitListener>(relaxed = true)
    payKit.registerForStateUpdates(listener)

    payKit.authorizeCustomerRequest(context, customerResponseData)

    verify { context.startActivity(any()) }
    verify { listener.payKitStateDidChange(Authorizing) }
  }
}
