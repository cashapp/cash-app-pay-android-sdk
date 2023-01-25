package app.cash.paykit.core

import app.cash.paykit.core.PayKitState.PayKitException
import app.cash.paykit.core.exceptions.PayKitApiNetworkException
import app.cash.paykit.core.exceptions.PayKitConnectivityNetworkException
import app.cash.paykit.core.impl.CashAppPayKitImpl
import app.cash.paykit.core.impl.NetworkManagerImpl
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonDataException
import io.mockk.MockKAnnotations
import io.mockk.mockk
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit.SECONDS

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

    val networkManager = NetworkManagerImpl(baseUrl = baseUrl.toString(), userAgentValue = "")
    val payKit = createPayKit(networkManager)
    val mockListener = MockListener()
    payKit.registerForStateUpdates(mockListener)

    payKit.createCustomerRequest(FakeData.oneTimePayment)

    // Verify that all the appropriate exception wrapping has occurred for a 503 error.
    assertThat(mockListener.state).isInstanceOf(PayKitException::class.java)
    assertThat((mockListener.state as PayKitException).exception).isInstanceOf(
      PayKitConnectivityNetworkException::class.java,
    )
    assertThat(((mockListener.state as PayKitException).exception as PayKitConnectivityNetworkException).e).isInstanceOf(
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
    val networkManager = NetworkManagerImpl(baseUrl = baseUrl.toString(), userAgentValue = "")
    val payKit = createPayKit(networkManager)
    val mockListener = MockListener()
    payKit.registerForStateUpdates(mockListener)

    payKit.createCustomerRequest(FakeData.oneTimePayment)

    // Verify that all the appropriate exception wrapping has occurred for a 400 error.
    assertTrue("Expected PayKitException end state", mockListener.state is PayKitException)
    assertTrue(
      "Expected exception abstraction to be PayKit",
      (mockListener.state as PayKitException).exception is PayKitApiNetworkException,
    )

    // Verify that all the API error details have been deserialized correctly.
    val apiError = (mockListener.state as PayKitException).exception as PayKitApiNetworkException
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

    val networkManager =
      NetworkManagerImpl(
        baseUrl = baseUrl.toString(),
        networkTimeoutMilliseconds = 1,
        userAgentValue = "",
      )
    val payKit = createPayKit(networkManager)
    val mockListener = MockListener()
    payKit.registerForStateUpdates(mockListener)

    payKit.createCustomerRequest(FakeData.oneTimePayment)

    // Verify that a timeout error was captured and relayed to the SDK listener.
    assertThat(((mockListener.state as PayKitException).exception as PayKitConnectivityNetworkException).e).isInstanceOf(
      SocketTimeoutException::class.java,
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
    val networkManager = NetworkManagerImpl(baseUrl = baseUrl.toString(), userAgentValue = "")
    val payKit = createPayKit(networkManager)
    val mockListener = MockListener()
    payKit.registerForStateUpdates(mockListener)

    payKit.createCustomerRequest(FakeData.oneTimePayment)

    // Verify that we got the appropriate JSON deserialization error.
    assertThat(mockListener.state).isInstanceOf(PayKitException::class.java)
    assertThat((mockListener.state as PayKitException).exception).isInstanceOf(JsonDataException::class.java)
  }

  /**
   * Our own Mock [CashAppPayKitListener] listener, that allows us to wait on a new state before continuing test execution.
   */
  internal class MockListener : CashAppPayKitListener {
    var state: PayKitState? = null

    override fun payKitStateDidChange(newState: PayKitState) {
      state = newState
    }
  }

  private fun createPayKit(networkManager: NetworkManager) =
    CashAppPayKitImpl(
      clientId = FakeData.CLIENT_ID,
      networkManager = networkManager,
      payKitLifecycleListener = mockk(relaxed = true),
      useSandboxEnvironment = true,
      analyticsService = mockk(relaxed = true),
    )
}
