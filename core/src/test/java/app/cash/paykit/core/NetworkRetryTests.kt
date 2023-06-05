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
import app.cash.paykit.core.CashAppPayState.ReadyToAuthorize
import app.cash.paykit.core.NetworkErrorTests.MockListener
import app.cash.paykit.core.exceptions.CashAppPayConnectivityNetworkException
import app.cash.paykit.core.impl.CashAppCashAppPayImpl
import app.cash.paykit.core.impl.NetworkManagerImpl
import app.cash.paykit.core.network.RetryManagerOptions
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test
import java.io.InterruptedIOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.time.Duration.Companion.milliseconds

@OptIn(kotlin.time.ExperimentalTime::class)
class NetworkRetryTests {

  private val MAX_RETRIES = 2

  @Test
  fun `failed network request will be retried and succeed`() {
    // Setup server & mock responses. First we fail, then we succeed.
    val server = MockWebServer()
    server.enqueue(MockResponse().setResponseCode(503))
    server.enqueue(
      MockResponse().setResponseCode(200).setHeader("content-type", "application/json")
        .setBody(FakeData.validCreateCustomerJSONresponse),
    )

    // Start the server.
    server.start()

    val baseUrl = server.url("")

    val networkManager = networkManager(baseUrl)
    val payKit = createPayKit(networkManager)
    val mockListener = MockListener()
    payKit.registerForStateUpdates(mockListener)

    payKit.createCustomerRequest(FakeData.oneTimePayment, FakeData.REDIRECT_URI)

    // If retry has happened, we got to a `ReadyToAuthorize` state.
    assertThat(mockListener.state).isInstanceOf(ReadyToAuthorize::class.java)
  }

  @Test
  fun `failed network request will still fail if retried enough times`() {
    // Setup server & mock responses. First we fail, then we succeed.
    val server = MockWebServer()
    server.enqueue(MockResponse().setResponseCode(503))

    // Have as many retries as specified all fail.
    for (i in 1..MAX_RETRIES) {
      server.enqueue(MockResponse().setResponseCode(503))
    }

    server.enqueue(
      MockResponse().setResponseCode(200).setHeader("content-type", "application/json")
        .setBody(FakeData.validCreateCustomerJSONresponse),
    ) // Retry 3 (shouldn't happen, since maxRetries = MAX_RETRIES)

    // Start the server.
    server.start()

    val baseUrl = server.url("")

    val networkManager = networkManager(baseUrl)
    val payKit = createPayKit(networkManager)
    val mockListener = MockListener()
    payKit.registerForStateUpdates(mockListener)

    payKit.createCustomerRequest(FakeData.oneTimePayment, FakeData.REDIRECT_URI)

    // We should retry twice, and then stop retrying. If the number of retries is correct,
    // we should have reached a `PayKitException` and NOT a `ReadyToAuthorize` state.
    assertThat(mockListener.state).isInstanceOf(CashAppPayExceptionState::class.java)
  }

  @Test
  fun `timeout issues will also be retried and able to succeed`() {
    // First request will timeout, second will succeed.
    val server = MockWebServer()
    server.enqueue(
      MockResponse().setResponseCode(200).setBodyDelay(10, SECONDS).setHeadersDelay(10, SECONDS),
    )
    server.enqueue(
      MockResponse().setResponseCode(200).setBodyDelay(1, TimeUnit.MILLISECONDS)
        .setHeadersDelay(1, TimeUnit.MILLISECONDS).setHeader("content-type", "application/json")
        .setBody(FakeData.validCreateCustomerJSONresponse),
    )

    // Start the server.
    server.start()
    val baseUrl = server.url("")

    val okHttpClient = OkHttpClient.Builder()
      .connectTimeout(100, TimeUnit.MILLISECONDS)
      .callTimeout(100, TimeUnit.MILLISECONDS)
      .readTimeout(100, TimeUnit.MILLISECONDS)
      .writeTimeout(100, TimeUnit.MILLISECONDS)
      .build()

    val networkManager = networkManager(baseUrl, okHttpClient)
    val payKit = createPayKit(networkManager)
    val mockListener = MockListener()
    payKit.registerForStateUpdates(mockListener)

    payKit.createCustomerRequest(FakeData.oneTimePayment, FakeData.REDIRECT_URI)

    // If retry has happened, we got to a `ReadyToAuthorize` state.
    assertThat(mockListener.state).isInstanceOf(ReadyToAuthorize::class.java)
  }

  @Test
  fun `consecutive timeout issues will be retried and still fail`() {
    // First request will timeout, second will succeed.
    val server = MockWebServer()

    for (i in 1..MAX_RETRIES + 1) {
      server.enqueue(
        MockResponse().setResponseCode(200).setBodyDelay(1, SECONDS)
          .setHeadersDelay(1, SECONDS).setHeader("content-type", "application/json")
          .setBody(FakeData.validCreateCustomerJSONresponse),
      )
    }

    // Start the server.
    server.start()
    val baseUrl = server.url("")

    val okHttpClient = OkHttpClient.Builder()
      .connectTimeout(1, TimeUnit.MILLISECONDS)
      .callTimeout(1, TimeUnit.MILLISECONDS)
      .readTimeout(1, TimeUnit.MILLISECONDS)
      .writeTimeout(1, TimeUnit.MILLISECONDS)
      .build()

    val networkManager = networkManager(baseUrl, okHttpClient)
    val payKit = createPayKit(networkManager)
    val mockListener = MockListener()
    payKit.registerForStateUpdates(mockListener)

    payKit.createCustomerRequest(FakeData.oneTimePayment, FakeData.REDIRECT_URI)

    // Verify that a timeout error was captured and relayed to the SDK listener.
    assertThat(((mockListener.state as CashAppPayExceptionState).exception as CashAppPayConnectivityNetworkException).e).isInstanceOf(
      InterruptedIOException::class.java,
    )
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
        maxRetries = MAX_RETRIES,
        initialDuration = 1.milliseconds,
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
