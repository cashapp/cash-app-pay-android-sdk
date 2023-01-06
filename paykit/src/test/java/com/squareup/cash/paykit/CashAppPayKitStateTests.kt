package com.squareup.cash.paykit

import android.content.Context
import com.squareup.cash.paykit.PayKitState.Authorizing
import com.squareup.cash.paykit.PayKitState.CreatingCustomerRequest
import com.squareup.cash.paykit.impl.CashAppPayKitImpl
import com.squareup.cash.paykit.models.response.CustomerResponseData
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CashAppPayKitStateTests {

  @MockK(relaxed = true)
  private lateinit var context: Context

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @After
  fun tearDown() {
    PayKitLifecycleObserver.reset()
  }

  @Test
  fun `CreatingCustomerRequest State`() {
    val payKit = createPayKit()
    val listener = mockk<CashAppPayKitListener>(relaxed = true)
    payKit.registerForStateUpdates(listener)
    payKit.createCustomerRequest(FakeData.oneTimePayment)
    verify { listener.payKitStateDidChange(CreatingCustomerRequest) }
  }

  @Test
  fun `UpdatingCustomerRequest State`() {
    // TODO: Re-write test once NetworkManager is injectable. Robolectric can't test PATCH requests: https://github.com/google/ExoPlayer/commit/df0e89c1678ff0dda00bb187be05b8198bd31567
    // val payKit = createPayKit()
    // val listener = mockk<CashAppPayKitListener>(relaxed = true)
    // payKit.registerForStateUpdates(listener)
    // payKit.updateCustomerRequest("abc", FakeData.oneTimePayment)
    // verify { listener.payKitStateDidChange(UpdatingCustomerRequest) }
  }

  // TODO: PollingTransactionStatus State test.
  // TODO: ReadyToAuthorize State test.
  // TODO: Approved State test.
  // TODO: Declined State test.

  @Test
  fun `Authorizing State`() {
    val payKit = createPayKit()
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

  private fun createPayKit() = CashAppPayKitImpl(FakeData.CLIENT_ID, useSandboxEnvironment = true)
}
