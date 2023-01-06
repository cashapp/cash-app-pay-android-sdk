package com.squareup.cash.paykit

import com.squareup.cash.paykit.exceptions.PayKitIntegrationException
import io.mockk.mockk
import org.junit.Test

class CashAppPayKitExceptionsTests {

  @Test(expected = PayKitIntegrationException::class)
  fun `should throw on createCustomerRequest if has NOT registered for state updates`() {
    val payKit = CashAppPayKit(FakeData.CLIENT_ID, useSandboxEnvironment = true)
    payKit.createCustomerRequest(FakeData.oneTimePayment)
  }

  @Test
  fun `logAndSoftCrash should NOT crash in prod`() {
    val payKit = CashAppPayKit(FakeData.CLIENT_ID, useSandboxEnvironment = false)
    val listener = mockk<CashAppPayKitListener>(relaxed = true)
    payKit.registerForStateUpdates(listener)
    payKit.createCustomerRequest(FakeData.oneTimePayment)
  }
}
