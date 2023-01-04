package com.squareup.cash.paykit

import com.squareup.cash.paykit.exceptions.PayKitIntegrationException
import com.squareup.cash.paykit.models.response.CustomerResponseData
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class CashAppPayKitExceptionsTests {

  @Test(expected = PayKitIntegrationException::class)
  fun `should throw if calling authorize before createCustomer`() {
    val payKit = CashAppPayKit(FakeData.CLIENT_ID, useSandboxEnvironment = true)
    val mockPayKitListener = mockk<CashAppPayKitListener>(relaxed = true)
    payKit.registerForStateUpdates(mockPayKitListener)
    val appContext = RuntimeEnvironment.getApplication()
    payKit.authorizeCustomerRequest(appContext)
  }

  @Test(expected = PayKitIntegrationException::class)
  fun `should throw on authorizeCustomerRequest if has NOT registered for state updates`() {
    val payKit = CashAppPayKit(FakeData.CLIENT_ID, useSandboxEnvironment = true)
    val customerResponseData = mockk<CustomerResponseData>(relaxed = true)
    val appContext = RuntimeEnvironment.getApplication()
    payKit.authorizeCustomerRequest(appContext, customerResponseData)
  }

  @Test(expected = PayKitIntegrationException::class)
  fun `should throw on createCustomerRequest if has NOT registered for state updates`() {
    val payKit = CashAppPayKit(FakeData.CLIENT_ID, useSandboxEnvironment = true)
    payKit.createCustomerRequest(FakeData.oneTimePayment)
  }

  fun `logAndSoftCrash should NOT crash in prod`() {
    val payKit = CashAppPayKit(FakeData.CLIENT_ID, useSandboxEnvironment = false)
    payKit.createCustomerRequest(FakeData.oneTimePayment)
  }
}
