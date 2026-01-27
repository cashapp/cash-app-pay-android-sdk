/*
 * Copyright (C) 2023 Cash App
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

import android.content.ActivityNotFoundException
import android.content.Context
import app.cash.paykit.core.CashAppPayState.Approved
import app.cash.paykit.core.CashAppPayState.Authorizing
import app.cash.paykit.core.CashAppPayState.CashAppPayExceptionState
import app.cash.paykit.core.CashAppPayState.CreatingCustomerRequest
import app.cash.paykit.core.CashAppPayState.Declined
import app.cash.paykit.core.CashAppPayState.NotStarted
import app.cash.paykit.core.CashAppPayState.PollingTransactionStatus
import app.cash.paykit.core.CashAppPayState.ReadyToAuthorize
import app.cash.paykit.core.CashAppPayState.UpdatingCustomerRequest
import app.cash.paykit.core.android.ApplicationContextHolder
import app.cash.paykit.core.fakes.FakeData
import app.cash.paykit.core.impl.CashAppPayImpl
import app.cash.paykit.core.impl.CashAppPayLifecycleListener
import app.cash.paykit.core.models.common.NetworkResult
import app.cash.paykit.core.models.response.CustomerResponseData
import app.cash.paykit.core.models.response.CustomerTopLevelResponse
import app.cash.paykit.core.models.response.STATUS_APPROVED
import app.cash.paykit.core.models.response.STATUS_PENDING
import app.cash.paykit.core.models.response.STATUS_PROCESSING
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class CashAppPayStateTests {

  @MockK(relaxed = true)
  private lateinit var context: Context

  @MockK(relaxed = true)
  private lateinit var networkManager: NetworkManager

  private val mockLifecycleListener = MockLifecycleListenerCashApp()

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @After
  fun teardown() {
    ApplicationContextHolder.clearApplicationRef()
  }

  @Test
  fun `CreatingCustomerRequest State`() {
    val payKit = createPayKit()
    val listener = mockk<CashAppPayListener>(relaxed = true)
    payKit.registerForStateUpdates(listener)

    every { networkManager.createCustomerRequest(any(), any(), any(), any()) } returns
      NetworkResult.failure(
        Exception("bad"),
      )

    payKit.createCustomerRequest(FakeData.oneTimePayment, FakeData.REDIRECT_URI)
    verify { listener.cashAppPayStateDidChange(CreatingCustomerRequest) }
  }

  @Test
  fun `UpdatingCustomerRequest State`() {
    val payKit = createPayKit()
    val listener = mockk<CashAppPayListener>(relaxed = true)
    payKit.registerForStateUpdates(listener)

    every {
      networkManager.updateCustomerRequest(
        any(),
        any(),
        any(),
        any(),
      )
    } returns NetworkResult.failure(
      Exception("bad"),
    )
    payKit.updateCustomerRequest("abc", FakeData.oneTimePayment)
    verify { listener.cashAppPayStateDidChange(UpdatingCustomerRequest) }
  }

  @Test
  fun `PollingTransactionStatus State`() {
    val payKit = createPayKit(Authorizing)
    val listener = mockk<CashAppPayListener>(relaxed = true)
    payKit.registerForStateUpdates(listener)
    mockLifecycleListener.simulateOnApplicationForegrounded()
    verify { listener.cashAppPayStateDidChange(PollingTransactionStatus) }
  }

  @Test
  fun `startWithExistingCustomerRequest fetches existing Approved request`() {
    val payKit = createPayKit()
    val listener = mockk<CashAppPayListener>(relaxed = true)
    payKit.registerForStateUpdates(listener)
    val customerTopLevelResponse: NetworkResult.Success<CustomerTopLevelResponse> = mockk()
    every { customerTopLevelResponse.data.customerResponseData.status } returns STATUS_APPROVED
    every {
      networkManager.retrieveUpdatedRequestData(
        any(),
        any(),
      )
    } returns customerTopLevelResponse

    payKit.startWithExistingCustomerRequest(FakeData.REQUEST_ID)
    verify { listener.cashAppPayStateDidChange(ofType(Approved::class)) }
  }

  @Test
  fun `startWithExistingCustomerRequest fetches existing Processing request`() {
    val payKit = createPayKit()
    val listener = mockk<CashAppPayListener>(relaxed = true)
    payKit.registerForStateUpdates(listener)
    val customerTopLevelResponse: NetworkResult.Success<CustomerTopLevelResponse> = mockk()
    every { customerTopLevelResponse.data.customerResponseData.status } returns STATUS_PROCESSING
    every {
      networkManager.retrieveUpdatedRequestData(
        any(),
        any(),
      )
    } returns customerTopLevelResponse

    payKit.startWithExistingCustomerRequest(FakeData.REQUEST_ID)
    verify { listener.cashAppPayStateDidChange(ofType(PollingTransactionStatus::class)) }
  }

  @Test
  fun `startWithExistingCustomerRequest fetches existing Pending request`() {
    val payKit = createPayKit()
    val listener = mockk<CashAppPayListener>(relaxed = true)
    payKit.registerForStateUpdates(listener)
    val customerTopLevelResponse: NetworkResult.Success<CustomerTopLevelResponse> = mockk()
    every { customerTopLevelResponse.data.customerResponseData.status } returns STATUS_PENDING
    every { customerTopLevelResponse.data.customerResponseData.authFlowTriggers } returns null
    every {
      networkManager.retrieveUpdatedRequestData(
        any(),
        any(),
      )
    } returns customerTopLevelResponse

    payKit.startWithExistingCustomerRequest(FakeData.REQUEST_ID)
    verify { listener.cashAppPayStateDidChange(ofType(ReadyToAuthorize::class)) }
  }

  @Test
  fun `ReadyToAuthorize State`() {
    val payKit = createPayKit()
    val listener = mockk<CashAppPayListener>(relaxed = true)
    payKit.registerForStateUpdates(listener)
    val customerTopLevelResponse: NetworkResult.Success<CustomerTopLevelResponse> = mockk()
    val customerResponseData: CustomerResponseData = mockk(relaxed = true)
    every { customerTopLevelResponse.data.customerResponseData } returns customerResponseData
    every {
      networkManager.createCustomerRequest(
        any(),
        any(),
        any(),
        any(),
      )
    } returns customerTopLevelResponse

    payKit.createCustomerRequest(FakeData.oneTimePayment, FakeData.REDIRECT_URI)
    verify { listener.cashAppPayStateDidChange(ofType(ReadyToAuthorize::class)) }
  }

  @Test
  fun `Approved State`() {
    val starterCustomerResponseData: CustomerResponseData = mockk(relaxed = true)
    val payKit = createPayKit(Authorizing, starterCustomerResponseData)
    val payKitListener = MockCashAppPayListener()
    payKit.registerForStateUpdates(payKitListener)

    // Mock necessary network response.
    val customerTopLevelResponse: NetworkResult.Success<CustomerTopLevelResponse> = mockk()
    val customerResponseData: CustomerResponseData = mockk()
    every { customerResponseData.status } returns "APPROVED"
    every { customerTopLevelResponse.data.customerResponseData } returns customerResponseData
    every {
      networkManager.retrieveUpdatedRequestData(
        any(),
        any(),
      )
    } returns customerTopLevelResponse

    // Initiate polling routine, and wait for thread to return.
    mockLifecycleListener.simulateOnApplicationForegrounded()
    synchronized(payKitListener) {
      (payKitListener as Object).wait()
    }

    // Verify we got the expected result.
    assertThat(payKitListener.state).isInstanceOf(Approved::class.java)
  }

  @Test
  fun `Authorizing State`() {
    every { context.startActivity(any()) } just runs
    setupAppHolder()
    val payKit = createPayKit()
    val customerResponseData = mockk<CustomerResponseData>(relaxed = true) {
      every { authFlowTriggers } returns mockk {
        every { mobileUrl } returns "http://url"
      }
    }
    val listener = mockk<CashAppPayListener>(relaxed = true)
    payKit.registerForStateUpdates(listener)

    payKit.authorizeCustomerRequest(customerResponseData)

    verify { listener.cashAppPayStateDidChange(Authorizing) }
  }

  @Test
  fun `Declined State`() {
    val starterCustomerResponseData: CustomerResponseData = mockk(relaxed = true)
    val payKit = createPayKit(Authorizing, starterCustomerResponseData)
    val payKitListener = MockCashAppPayListener()
    payKit.registerForStateUpdates(payKitListener)

    // Mock necessary network response.
    val customerTopLevelResponse: NetworkResult.Success<CustomerTopLevelResponse> = mockk()
    val customerResponseData: CustomerResponseData = mockk()
    every { customerResponseData.status } returns "DECLINED"
    every { customerTopLevelResponse.data.customerResponseData } returns customerResponseData
    every {
      networkManager.retrieveUpdatedRequestData(
        any(),
        any(),
      )
    } returns customerTopLevelResponse

    // Initiate polling routine, and wait for thread to return.
    mockLifecycleListener.simulateOnApplicationForegrounded()
    synchronized(payKitListener) {
      (payKitListener as Object).wait()
    }

    // Verify we got the expected result.
    assertThat(payKitListener.state).isInstanceOf(Declined::class.java)
  }

  @Test
  fun `fail to Authorize if mobileUrl cannot be opened by the system`() {
    val payKit = createPayKit()
    val customerResponseData = mockk<CustomerResponseData>(relaxed = true) {
      every { authFlowTriggers } returns mockk {
        every { mobileUrl } returns "cashme://url"
      }
    }
    val listener = mockk<CashAppPayListener>(relaxed = true)
    payKit.registerForStateUpdates(listener)

    every { context.startActivity(any()) } throws ActivityNotFoundException("yoh")
    setupAppHolder()
    payKit.authorizeCustomerRequest(customerResponseData)

    verify { listener.cashAppPayStateDidChange(ofType(CashAppPayExceptionState::class)) }
  }

  private fun createPayKit(
    initialState: CashAppPayState = NotStarted,
    initialCustomerResponseData: CustomerResponseData? = null,
  ) = CashAppPayImpl(
    clientId = FakeData.CLIENT_ID,
    networkManager = networkManager,
    payKitLifecycleListener = mockLifecycleListener,
    useSandboxEnvironment = true,
    initialState = initialState,
    initialCustomerResponseData = initialCustomerResponseData,
    analyticsEventDispatcher = mockk(relaxed = true),
    logger = mockk(relaxed = true),
  )

  /**
   * Specialized Mock [CashAppPayLifecycleObserver] that we can easily simulate the following events:
   * - `onApplicationForegrounded`
   * - `onApplicationBackgrounded`
   */
  private class MockLifecycleListenerCashApp : CashAppPayLifecycleObserver {
    private var listener: CashAppPayLifecycleListener? = null

    fun simulateOnApplicationForegrounded() {
      listener?.onApplicationForegrounded()
    }

    override fun register(newInstance: CashAppPayLifecycleListener) {
      listener = newInstance
    }

    override fun unregister(instanceToRemove: CashAppPayLifecycleListener) {
      listener = null
    }
  }

  private fun setupAppHolder() {
    every { context.applicationContext } returns context
    ApplicationContextHolder.init(context)
  }

  /**
   * Our own Mock [CashAppPayListener] listener, that allows us to wait on a new state before continuing test execution.
   */
  internal class MockCashAppPayListener : CashAppPayListener {
    var state: CashAppPayState? = null

    override fun cashAppPayStateDidChange(newState: CashAppPayState) {
      state = newState
      synchronized(this) { (this as Object).notifyAll() }
    }
  }
}
