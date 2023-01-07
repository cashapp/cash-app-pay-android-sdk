package com.squareup.cash.paykit

import com.squareup.cash.paykit.exceptions.PayKitIntegrationException
import com.squareup.cash.paykit.impl.CashAppPayKitImpl
import com.squareup.cash.paykit.models.common.NetworkResult
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class CashAppPayKitExceptionsTests {

  @MockK(relaxed = true)
  private lateinit var networkManager: NetworkManager

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test(expected = PayKitIntegrationException::class)
  fun `should throw on createCustomerRequest if has NOT registered for state updates`() {
    val payKit = createPayKit(useSandboxEnvironment = true)
    payKit.createCustomerRequest(FakeData.oneTimePayment)
  }

  @Test
  fun `logAndSoftCrash should NOT crash in prod`() {
    val payKit = createPayKit(useSandboxEnvironment = false)
    val listener = mockk<CashAppPayKitListener>(relaxed = true)
    payKit.registerForStateUpdates(listener)

    every { networkManager.createCustomerRequest(any(), any()) } returns NetworkResult.failure(
      Exception("bad"),
    )
    payKit.createCustomerRequest(FakeData.oneTimePayment)
  }

  private fun createPayKit(useSandboxEnvironment: Boolean) =
    CashAppPayKitImpl(
      clientId = FakeData.CLIENT_ID,
      networkManager = networkManager,
      payKitLifecycleListener = mockk(),
      useSandboxEnvironment = useSandboxEnvironment,
    )
}
