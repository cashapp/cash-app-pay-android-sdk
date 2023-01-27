package app.cash.paykit.core.analytics

import EventStream2Event
import app.cash.paykit.core.NetworkManager
import app.cash.paykit.core.PayKitState
import app.cash.paykit.core.PayKitState.PayKitException
import app.cash.paykit.core.models.analytics.payloads.AnalyticsInitializationPayload
import app.cash.paykit.core.models.response.CustomerResponseData
import app.cash.paykit.core.models.sdk.PayKitPaymentAction
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import java.util.UUID

private const val APP_NAME = "paykitsdk-android"
private const val PLATFORM = "android"

@OptIn(ExperimentalStdlibApi::class)
internal class PayKitAnalyticsEventsImpl(
  private val sdkVersion: String,
  private val clientId: String,
  private val userAgent: String,
  private val networkManager: NetworkManager, // TODO : to be removed.
) : PayKitAnalyticsEvents {

  override fun sdkInitialized() {
    // Inner payload of the ES2 event.
    val initializationPayload =
      AnalyticsInitializationPayload(sdkVersion, userAgent, PLATFORM, clientId)

    val moshi: Moshi = Moshi.Builder().build()
    val moshiAdapter: JsonAdapter<AnalyticsInitializationPayload> = moshi.adapter()
    val jsonData: String = moshiAdapter.toJson(initializationPayload)

    // ES2 event data class.
    val eventStream2Event =
      EventStream2Event(
        appName = APP_NAME,
        catalogName = AnalyticsInitializationPayload.CATALOG,
        uuid = UUID.randomUUID().toString(),
        jsonData = jsonData,
        recordedAt = System.nanoTime() * 10,
      )

    // Transform ES2 event into a JSON String.
    val es2EventAdapter: JsonAdapter<EventStream2Event> = moshi.adapter()
    val es2EventAsJsonString: String = es2EventAdapter.toJson(eventStream2Event)

    // Schedule event to be sent.
    sendAnalyticsEvent(es2EventAsJsonString)
  }

  override fun eventListenerAdded() {
    TODO("Not yet implemented")
  }

  override fun eventListenerRemoved() {
    TODO("Not yet implemented")
  }

  override fun createdCustomerRequest(
    action: PayKitPaymentAction,
    channel: String,
    redirectUrl: String,
    referenceId: String?,
  ) {
    TODO("Not yet implemented")
  }

  override fun genericStateChanged(
    payKitState: PayKitState,
    customerResponseData: CustomerResponseData,
  ) {
    TODO("Not yet implemented")
  }

  override fun exceptionOccurred(payKitException: PayKitException) {
    TODO("Not yet implemented")
  }

  /**
   * TODO: This entire function should be replaced with a call to the analytics scheduler instead. Here for demonstration purposes.
   */
  private fun sendAnalyticsEvent(jsonPayload: String) {
    Thread {
      networkManager.uploadAnalyticsEvents(listOf(jsonPayload))
    }.start()
  }
}
