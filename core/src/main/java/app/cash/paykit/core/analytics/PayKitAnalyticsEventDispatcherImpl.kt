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
package app.cash.paykit.core.analytics

import EventStream2Event
import app.cash.paykit.analytics.PayKitAnalytics
import app.cash.paykit.analytics.core.DeliveryHandler
import app.cash.paykit.analytics.core.DeliveryListener
import app.cash.paykit.analytics.persistence.AnalyticEntry
import app.cash.paykit.core.CashAppPayState
import app.cash.paykit.core.CashAppPayState.Approved
import app.cash.paykit.core.CashAppPayState.Authorizing
import app.cash.paykit.core.CashAppPayState.CashAppPayExceptionState
import app.cash.paykit.core.CashAppPayState.CreatingCustomerRequest
import app.cash.paykit.core.CashAppPayState.Declined
import app.cash.paykit.core.CashAppPayState.NotStarted
import app.cash.paykit.core.CashAppPayState.PollingTransactionStatus
import app.cash.paykit.core.CashAppPayState.ReadyToAuthorize
import app.cash.paykit.core.CashAppPayState.RetrievingExistingCustomerRequest
import app.cash.paykit.core.CashAppPayState.UpdatingCustomerRequest
import app.cash.paykit.core.NetworkManager
import app.cash.paykit.core.analytics.EventStream2Event.Companion.ESEventType
import app.cash.paykit.core.exceptions.CashAppCashAppPayApiNetworkException
import app.cash.paykit.core.models.analytics.payloads.AnalyticsBasePayload
import app.cash.paykit.core.models.analytics.payloads.AnalyticsCustomerRequestPayload
import app.cash.paykit.core.models.analytics.payloads.AnalyticsEventListenerPayload
import app.cash.paykit.core.models.analytics.payloads.AnalyticsInitializationPayload
import app.cash.paykit.core.models.common.Action
import app.cash.paykit.core.models.common.NetworkResult.Failure
import app.cash.paykit.core.models.common.NetworkResult.Success
import app.cash.paykit.core.models.request.CustomerRequestDataFactory.CHANNEL_IN_APP
import app.cash.paykit.core.models.response.CustomerResponseData
import app.cash.paykit.core.models.response.Grant
import app.cash.paykit.core.models.sdk.CashAppPayPaymentAction
import app.cash.paykit.core.models.sdk.CashAppPayPaymentAction.OnFileAction
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.*

private const val APP_NAME = "paykitsdk-android"
private const val PLATFORM = "android"

@OptIn(ExperimentalStdlibApi::class)
internal class PayKitAnalyticsEventDispatcherImpl(
  private val sdkVersion: String,
  private val clientId: String,
  private val userAgent: String,
  private val sdkEnvironment: String,
  private val payKitAnalytics: PayKitAnalytics,
  private val networkManager: NetworkManager,
  private val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build(),
) : PayKitAnalyticsEventDispatcher {

  private var eventStreamDeliverHandler: DeliveryHandler? = null

  init {
    eventStreamDeliverHandler = object : DeliveryHandler() {

      override val deliverableType = ESEventType

      override fun deliver(entries: List<AnalyticEntry>, deliveryListener: DeliveryListener) {
        val eventsAsJson = entries.map { it.content }
        when (networkManager.uploadAnalyticsEvents(eventsAsJson)) {
          is Failure -> deliveryListener.onError(entries)
          is Success -> deliveryListener.onSuccess(entries)
        }
      }
    }

    eventStreamDeliverHandler?.let {
      payKitAnalytics.registerDeliveryHandler(it)
    }
  }

  override fun sdkInitialized() {
    // Inner payload of the ES2 event.
    val initializationPayload =
      AnalyticsInitializationPayload(sdkVersion, userAgent, PLATFORM, clientId, sdkEnvironment)

    val es2EventAsJsonString =
      encodeToJsonString(initializationPayload, AnalyticsInitializationPayload.CATALOG)

    // Schedule event to be sent.
    payKitAnalytics.scheduleForDelivery(EventStream2Event(es2EventAsJsonString))
  }

  override fun eventListenerAdded() {
    // Inner payload of the ES2 event.
    val eventPayload =
      AnalyticsEventListenerPayload(sdkVersion, userAgent, PLATFORM, clientId, isAdded = true, environment = sdkEnvironment)

    val es2EventAsJsonString =
      encodeToJsonString(eventPayload, AnalyticsEventListenerPayload.CATALOG)
    payKitAnalytics.scheduleForDelivery(EventStream2Event(es2EventAsJsonString))
  }

  override fun eventListenerRemoved() {
    // Inner payload of the ES2 event.
    val eventPayload =
      AnalyticsEventListenerPayload(sdkVersion, userAgent, PLATFORM, clientId, isAdded = false, environment = sdkEnvironment)

    val es2EventAsJsonString =
      encodeToJsonString(eventPayload, AnalyticsEventListenerPayload.CATALOG)
    payKitAnalytics.scheduleForDelivery(EventStream2Event(es2EventAsJsonString))
  }

  override fun createdCustomerRequest(
    paymentKitActions: List<CashAppPayPaymentAction>,
    apiActions: List<Action>,
    redirectUri: String?,
  ) {
    val eventPayload = createOrUpdateAnalyticsPayload(paymentKitActions, apiActions, null, redirectUri)

    val es2EventAsJsonString =
      encodeToJsonString(eventPayload, AnalyticsCustomerRequestPayload.CATALOG)
    payKitAnalytics.scheduleForDelivery(EventStream2Event(es2EventAsJsonString))
  }

  override fun updatedCustomerRequest(
    requestId: String,
    paymentKitActions: List<CashAppPayPaymentAction>,
    apiActions: List<Action>,
  ) {
    val eventPayload = createOrUpdateAnalyticsPayload(paymentKitActions, apiActions, requestId, null)

    val es2EventAsJsonString =
      encodeToJsonString(eventPayload, AnalyticsCustomerRequestPayload.CATALOG)
    payKitAnalytics.scheduleForDelivery(EventStream2Event(es2EventAsJsonString))
  }

  override fun genericStateChanged(
    cashAppPayState: CashAppPayState,
    customerResponseData: CustomerResponseData?,
  ) {
    val eventPayload =
      eventFromCustomerResponseData(customerResponseData).copy(action = stateToAnalyticsAction(cashAppPayState))
    val es2EventAsJsonString =
      encodeToJsonString(eventPayload, AnalyticsCustomerRequestPayload.CATALOG)
    payKitAnalytics.scheduleForDelivery(EventStream2Event(es2EventAsJsonString))
  }

  override fun stateApproved(
    approved: Approved,
  ) {
    val eventPayload =
      eventFromCustomerResponseData(approved.responseData).copy(action = stateToAnalyticsAction(approved))
    val es2EventAsJsonString =
      encodeToJsonString(eventPayload, AnalyticsCustomerRequestPayload.CATALOG)
    payKitAnalytics.scheduleForDelivery(EventStream2Event(es2EventAsJsonString))
  }

  override fun exceptionOccurred(
    payKitExceptionState: CashAppPayExceptionState,
    customerResponseData: CustomerResponseData?,
  ) {
    var eventPayload =
      eventFromCustomerResponseData(customerResponseData).copy(action = stateToAnalyticsAction(payKitExceptionState))

    eventPayload = if (payKitExceptionState.exception is CashAppCashAppPayApiNetworkException) {
      val apiError = payKitExceptionState.exception
      eventPayload.copy(
        errorCode = apiError.code,
        errorCategory = apiError.category,
        errorField = apiError.field_value,
        errorDetail = apiError.detail,
      )
    } else {
      eventPayload.copy(
        errorCode = payKitExceptionState.exception.cause?.toString(),
        errorDetail = payKitExceptionState.exception.message,
      )
    }

    val es2EventAsJsonString =
      encodeToJsonString(eventPayload, AnalyticsCustomerRequestPayload.CATALOG)
    payKitAnalytics.scheduleForDelivery(EventStream2Event(es2EventAsJsonString))
  }

  override fun shutdown() {
    payKitAnalytics.scheduleShutdown()
  }

  private fun createOrUpdateAnalyticsPayload(
    paymentKitActions: List<CashAppPayPaymentAction>,
    apiActions: List<Action>,
    requestId: String?,
    redirectUri: String?,
  ): AnalyticsCustomerRequestPayload {
    val isUpdate = requestId != null
    val actionType = if (isUpdate) {
      UpdatingCustomerRequest
    } else {
      CreatingCustomerRequest
    }

    val moshiAdapter: JsonAdapter<List<Action>> = moshi.adapter()
    val apiActionsAsJson: String = moshiAdapter.toJson(apiActions)

    // Inner payload of the ES2 event.
    var possibleReferenceId: String? = null
    for (paymentAction in paymentKitActions) {
      if (paymentAction is OnFileAction) {
        possibleReferenceId = paymentAction.accountReferenceId
      }
    }

    return AnalyticsCustomerRequestPayload(
      sdkVersion,
      userAgent,
      PLATFORM,
      clientId,
      action = stateToAnalyticsAction(actionType),
      createActions = apiActionsAsJson,
      createChannel = CHANNEL_IN_APP,
      createRedirectUrl = redirectUri,
      createReferenceId = possibleReferenceId,
      environment = sdkEnvironment,
    )
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

  private fun eventFromCustomerResponseData(customerResponseData: CustomerResponseData?): AnalyticsCustomerRequestPayload {
    var grantsPayload: String? = null
    if (customerResponseData?.grants != null) {
      val moshiAdapter: JsonAdapter<List<Grant>> = moshi.adapter()
      grantsPayload = moshiAdapter.toJson(customerResponseData.grants)
    }

    return AnalyticsCustomerRequestPayload(
      sdkVersion,
      userAgent,
      PLATFORM,
      clientId,
      status = customerResponseData?.status,
      authMobileUrl = customerResponseData?.authFlowTriggers?.mobileUrl,
      updatedAt = customerResponseData?.updatedAt?.toLongOrNull(),
      createdAt = customerResponseData?.createdAt?.toLongOrNull(),
      originType = customerResponseData?.origin?.type,
      originId = customerResponseData?.origin?.id,
      requestChannel = CHANNEL_IN_APP,
      approvedGrants = grantsPayload,
      customerId = customerResponseData?.customerProfile?.id,
      customerCashTag = customerResponseData?.customerProfile?.cashTag,
      requestId = customerResponseData?.id,
      referenceId = customerResponseData?.referenceId,
      environment = sdkEnvironment,
    )
  }

  /**
   * This function converts a [CashAppPayState] into a valid String action for analytics ingestion.
   */
  private fun stateToAnalyticsAction(state: CashAppPayState): String {
    return when (state) {
      is Approved -> "approved"
      Authorizing -> "redirect"
      CreatingCustomerRequest -> "create"
      Declined -> "declined"
      NotStarted -> "not_started"
      is CashAppPayExceptionState -> "paykit_exception"
      PollingTransactionStatus -> "polling"
      is ReadyToAuthorize -> "ready_to_authorize"
      RetrievingExistingCustomerRequest -> "retrieve_existing_customer_request"
      UpdatingCustomerRequest -> "update"
    }
  }
}
