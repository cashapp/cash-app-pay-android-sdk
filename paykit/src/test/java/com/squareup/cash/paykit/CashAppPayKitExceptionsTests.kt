package com.squareup.cash.paykit

import com.squareup.cash.paykit.exceptions.PayKitIntegrationException
import org.junit.Test

class CashAppPayKitExceptionsTests {

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
