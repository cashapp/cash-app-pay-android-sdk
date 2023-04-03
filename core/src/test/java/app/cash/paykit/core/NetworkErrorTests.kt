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

import app.cash.paykit.core.CashAppPayState.CashAppPayExceptionState
import app.cash.paykit.core.exceptions.CashAppCashAppPayApiNetworkException
import app.cash.paykit.core.exceptions.CashAppPayConnectivityNetworkException
import app.cash.paykit.core.impl.CashAppCashAppPayImpl
import app.cash.paykit.core.impl.NetworkManagerImpl
import app.cash.paykit.core.network.RetryManagerOptions
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonDataException
import io.mockk.MockKAnnotations
import io.mockk.mockk
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class NetworkErrorTests {

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `HTTP code without payload should be wrapped with correct SDK defined exception`() {
    // Setup server & mock responses.
    val server = MockWebServer()
    server.enqueue(MockResponse().setResponseCode(503))

    // Start the server.
    server.start()

    val baseUrl = server.url("")

    val networkManager = networkManager(baseUrl)
    val payKit = createPayKit(networkManager)
    val mockListener = MockListener()
    payKit.registerForStateUpdates(mockListener)

    payKit.createCustomerRequest(FakeData.REDIRECT_URI, FakeData.oneTimePayment)

    // Verify that all the appropriate exception wrapping has occurred for a 503 error.
    assertThat(mockListener.state).isInstanceOf(CashAppPayExceptionState::class.java)
    assertThat((mockListener.state as CashAppPayExceptionState).exception).isInstanceOf(
      CashAppPayConnectivityNetworkException::class.java,
    )
    assertThat(((mockListener.state as CashAppPayExceptionState).exception as CashAppPayConnectivityNetworkException).e).isInstanceOf(
      IOException::class.java,
    )
  }

  @Test
  fun `HTTP error code with payload should be wrapped with correct SDK defined exception and contains API deserialized data`() {
    // Setup server & mock responses.
    val server = MockWebServer()
    server.enqueue(
      MockResponse().setResponseCode(400).setHeader("content-type", "application/json").setBody(
        """{
  "errors": [
    {
      "category": "INVALID_REQUEST_ERROR",
      "code": "MISSING_REQUIRED_PARAMETER",
      "detail": "One time payments require amount and currency to both be set or unset; one cannot be null while the other is present.",
      "field": "request.action.amount"
    }
  ]
}""",
      ),
    )

    // Start the server.
    server.start()

    val baseUrl = server.url("")
    val networkManager = networkManager(baseUrl)
    val payKit = createPayKit(networkManager)
    val mockListener = MockListener()
    payKit.registerForStateUpdates(mockListener)

    payKit.createCustomerRequest(FakeData.REDIRECT_URI, FakeData.oneTimePayment)

    // Verify that all the appropriate exception wrapping has occurred for a 400 error.
    assertThat(mockListener.state).isInstanceOf(CashAppPayExceptionState::class.java)
    assertThat((mockListener.state as CashAppPayExceptionState).exception).isInstanceOf(
      CashAppCashAppPayApiNetworkException::class.java,
    )

    // Verify that all the API error details have been deserialized correctly.
    val apiError = (mockListener.state as CashAppPayExceptionState).exception as CashAppCashAppPayApiNetworkException
    assertThat(apiError.code).isEqualTo("MISSING_REQUIRED_PARAMETER")
    assertThat(apiError.category).isEqualTo("INVALID_REQUEST_ERROR")
    assertThat(apiError.field_value).isEqualTo("request.action.amount")
  }

  @Test
  fun `network request timeout should be wrapped with correct SDK defined exception`() {
    // Setup server & mock responses.
    val server = MockWebServer()
    server.enqueue(
      MockResponse().setResponseCode(200).setBodyDelay(10, SECONDS).setHeadersDelay(10, SECONDS),
    )

    // Start the server.
    server.start()
    val baseUrl = server.url("")

    val okHttpClient = OkHttpClient.Builder()
      .connectTimeout(1, MILLISECONDS)
      .callTimeout(1, MILLISECONDS)
      .readTimeout(1, MILLISECONDS)
      .writeTimeout(1, MILLISECONDS)
      .build()

    val networkManager = networkManager(baseUrl, okHttpClient)
    val payKit = createPayKit(networkManager)
    val mockListener = MockListener()
    payKit.registerForStateUpdates(mockListener)

    payKit.createCustomerRequest(FakeData.REDIRECT_URI, FakeData.oneTimePayment)

    // Verify that a timeout error was captured and relayed to the SDK listener.
    assertThat(((mockListener.state as CashAppPayExceptionState).exception as CashAppPayConnectivityNetworkException).e).isInstanceOf(
      InterruptedIOException::class.java,
    )
  }

  @Test
  fun `unsupported json payload should be wrapped in corresponding SDK exception`() {
    // Setup server & mock responses.
    val server = MockWebServer()

    // Below response is an invalid JSON, because `id` should NOT be `null`.
    server.enqueue(
      MockResponse().setResponseCode(200).setHeader("content-type", "application/json").setBody(
        """{
  "request": {
    "id": null,
    "status": "PENDING",
    "actions": [
      {
        "type": "ONE_TIME_PAYMENT",
        "amount": 500,
        "currency": "USD",
        "scope_id": "BRAND_9kx6p0mkuo97jnl025q9ni94t"
      }
    ],
    "origin": {
      "type": "DIRECT"
    },
    "auth_flow_triggers": {
      "qr_code_image_url": "https://sandbox.api.cash.app/qr/sandbox/v1/GRR_k1xx5sp48azs73kqgkpdqpff-rxb309?rounded=0&format=png",
      "qr_code_svg_url": "https://sandbox.api.cash.app/qr/sandbox/v1/GRR_k1xx5sp48azs73kqgkpdqpff-rxb309?rounded=0&format=svg",
      "mobile_url": "https://sandbox.api.cash.app/customer-request/v1/requests/GRR_k1xx5sp48azs73kqgkpdqpff/interstitial?validity_token=rxb309",
      "refreshes_at": "2022-11-11T19:28:25.451Z"
    },
    "created_at": "2022-11-11T19:27:55.475Z",
    "updated_at": "2022-11-11T19:27:55.475Z",
    "expires_at": "2022-11-11T20:27:55.451Z",
    "requester_profile": {
      "name": "SDK Hacking: The Brand",
      "logo_url": "defaultlogo.jpg"
    },
    "channel": "IN_APP"
  }
}""",
      ),
    )

    // Start the server.
    server.start()

    val baseUrl = server.url("")
    val networkManager = networkManager(baseUrl)
    val payKit = createPayKit(networkManager)
    val mockListener = MockListener()
    payKit.registerForStateUpdates(mockListener)

    payKit.createCustomerRequest(FakeData.REDIRECT_URI, FakeData.oneTimePayment)

    // Verify that we got the appropriate JSON deserialization error.
    assertThat(mockListener.state).isInstanceOf(CashAppPayExceptionState::class.java)
    assertThat((mockListener.state as CashAppPayExceptionState).exception).isInstanceOf(JsonDataException::class.java)
  }

  /**
   * Our own Mock [CashAppPayListener] listener, that allows us to wait on a new state before continuing test execution.
   */
  internal class MockListener : CashAppPayListener {
    var state: CashAppPayState? = null

    override fun cashAppPayStateDidChange(newState: CashAppPayState) {
      state = newState
    }
  }

  private fun networkManager(
    baseUrl: HttpUrl,
    okHttpClient: OkHttpClient = OkHttpClient(),
  ): NetworkManager {
    return NetworkManagerImpl(
      baseUrl = baseUrl.toString(),
      userAgentValue = "",
      okHttpClient = okHttpClient,
      retryManagerOptions = RetryManagerOptions(
        maxRetries = 1,
        initialDuration = 1.toDuration(DurationUnit.MILLISECONDS),
      ),
      analyticsBaseUrl = "",
    )
  }

  private fun createPayKit(networkManager: NetworkManager) =
    CashAppCashAppPayImpl(
      clientId = FakeData.CLIENT_ID,
      networkManager = networkManager,
      payKitLifecycleListener = mockk(relaxed = true),
      useSandboxEnvironment = true,
      analyticsEventDispatcher = mockk(relaxed = true),
    )
}
