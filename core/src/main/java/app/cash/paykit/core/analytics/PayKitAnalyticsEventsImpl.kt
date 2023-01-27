package app.cash.paykit.core.analytics

import EventStream2Event
import app.cash.paykit.core.NetworkManager
import app.cash.paykit.core.PayKitState
import app.cash.paykit.core.PayKitState.PayKitException
import app.cash.paykit.core.models.analytics.payloads.AnalyticsBasePayload
import app.cash.paykit.core.models.analytics.payloads.AnalyticsCustomerRequestPayload
import app.cash.paykit.core.models.analytics.payloads.AnalyticsEventListenerPayload
import app.cash.paykit.core.models.analytics.payloads.AnalyticsInitializationPayload
import app.cash.paykit.core.models.request.CustomerRequestDataFactory.CHANNEL_IN_APP
import app.cash.paykit.core.models.response.CustomerResponseData
import app.cash.paykit.core.models.sdk.PayKitPaymentAction
import app.cash.paykit.core.models.sdk.PayKitPaymentAction.OnFileAction
import app.cash.paykit.core.models.sdk.PayKitPaymentAction.OneTimeAction
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
  private val moshi: Moshi = Moshi.Builder().build(),
) : PayKitAnalyticsEvents {

  override fun sdkInitialized() {
    // Inner payload of the ES2 event.
    val initializationPayload =
      AnalyticsInitializationPayload(sdkVersion, userAgent, PLATFORM, clientId)

    val es2EventAsJsonString =
      encodeToJsonString(initializationPayload, AnalyticsInitializationPayload.CATALOG)

    // Schedule event to be sent.
    sendAnalyticsEvent(es2EventAsJsonString)
  }

  override fun eventListenerAdded() {
    // Inner payload of the ES2 event.
    val eventPayload =
      AnalyticsEventListenerPayload(sdkVersion, userAgent, PLATFORM, clientId, isAdded = true)

    val es2EventAsJsonString =
      encodeToJsonString(eventPayload, AnalyticsEventListenerPayload.CATALOG)
    sendAnalyticsEvent(es2EventAsJsonString)
  }

  override fun eventListenerRemoved() {
    // Inner payload of the ES2 event.
    val eventPayload =
      AnalyticsEventListenerPayload(sdkVersion, userAgent, PLATFORM, clientId, isAdded = false)

    val es2EventAsJsonString =
      encodeToJsonString(eventPayload, AnalyticsEventListenerPayload.CATALOG)
    sendAnalyticsEvent(es2EventAsJsonString)
  }

  override fun createdCustomerRequest(
    action: PayKitPaymentAction,
  ) {
    // TODO: How easy would it be to convert `PayKitPaymentAction` to JSON ?

    // Inner payload of the ES2 event.
    val eventPayload = when (action) {
      is OnFileAction -> {
        AnalyticsCustomerRequestPayload(
          sdkVersion,
          userAgent,
          PLATFORM,
          clientId,
          action = PayKitState.CreatingCustomerRequest.toString(),
          createActions = action.toString(),
          createChannel = CHANNEL_IN_APP,
          createRedirectUrl = action.redirectUri,
          createReferenceId = action.accountReferenceId,
        )
      }

      is OneTimeAction -> {
        AnalyticsCustomerRequestPayload(
          sdkVersion,
          userAgent,
          PLATFORM,
          clientId,
          action = PayKitState.CreatingCustomerRequest.toString(),
          createActions = action.toString(),
          createChannel = CHANNEL_IN_APP,
          createRedirectUrl = action.redirectUri,
          createReferenceId = null,
        )
      }
    }

    val es2EventAsJsonString =
      encodeToJsonString(eventPayload, AnalyticsCustomerRequestPayload.CATALOG)
    sendAnalyticsEvent(es2EventAsJsonString)
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

  private inline fun <reified In : AnalyticsBasePayload> encodeToJsonString(
    payload: In,
    catalog: String,
  ): String {
    val moshiAdapter: JsonAdapter<In> = moshi.adapter()
    val jsonData: String = moshiAdapter.toJson(payload)

    // ES2 event data class.
    val eventStream2Event =
      EventStream2Event(
        appName = APP_NAME,
        catalogName = catalog,
        uuid = UUID.randomUUID().toString(),
        jsonData = jsonData,
        recordedAt = System.nanoTime() * 10,
      )

    // Transform ES2 event into a JSON String.
    val es2EventAdapter: JsonAdapter<EventStream2Event> = moshi.adapter()
    return es2EventAdapter.toJson(eventStream2Event)
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
