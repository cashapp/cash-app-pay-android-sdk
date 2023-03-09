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
package app.cash.paykit.core.impl

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.WorkerThread
import app.cash.paykit.core.BuildConfig
import app.cash.paykit.core.CashAppPay
import app.cash.paykit.core.CashAppPayLifecycleObserver
import app.cash.paykit.core.CashAppPayListener
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
import app.cash.paykit.core.analytics.PayKitAnalyticsEventDispatcher
import app.cash.paykit.core.android.ApplicationContextHolder
import app.cash.paykit.core.exceptions.CashAppPayIntegrationException
import app.cash.paykit.core.models.common.NetworkResult.Failure
import app.cash.paykit.core.models.common.NetworkResult.Success
import app.cash.paykit.core.models.response.CustomerResponseData
import app.cash.paykit.core.models.response.STATUS_APPROVED
import app.cash.paykit.core.models.response.STATUS_PENDING
import app.cash.paykit.core.models.sdk.CashAppPayPaymentAction
import app.cash.paykit.core.utils.orElse

/**
 * @param clientId Client Identifier that should be provided by Cash PayKit integration.
 * @param useSandboxEnvironment Specify what development environment should be used.
 */
internal class CashAppCashAppPayImpl(
  private val clientId: String,
  private val networkManager: NetworkManager,
  private val analyticsEventDispatcher: PayKitAnalyticsEventDispatcher,
  private val payKitLifecycleListener: CashAppPayLifecycleObserver,
  private val useSandboxEnvironment: Boolean = false,
  initialState: CashAppPayState = NotStarted,
  initialCustomerResponseData: CustomerResponseData? = null,
) : CashAppPay, CashAppPayLifecycleListener {

  // TODO: Check if a given API call is allowed against a given internal SDK state. ( https://www.notion.so/cashappcash/Check-if-a-given-API-call-is-allowed-against-current-internal-SDK-state-0073051cd5aa42c7b9672542e9576f85 )

  private var callbackListener: CashAppPayListener? = null

  private var customerResponseData: CustomerResponseData? = initialCustomerResponseData

  private var currentState: CashAppPayState = initialState
    set(value) {
      field = value
      // Track Analytics for various state changes.
      when (value) {
        is Approved -> analyticsEventDispatcher.stateApproved(value)
        is CashAppPayExceptionState -> analyticsEventDispatcher.exceptionOccurred(
          value,
          customerResponseData,
        )
        Authorizing -> analyticsEventDispatcher.genericStateChanged(value, customerResponseData)
        Declined -> analyticsEventDispatcher.genericStateChanged(value, customerResponseData)
        NotStarted -> analyticsEventDispatcher.genericStateChanged(value, customerResponseData)
        PollingTransactionStatus -> analyticsEventDispatcher.genericStateChanged(value, customerResponseData)
        is ReadyToAuthorize -> analyticsEventDispatcher.genericStateChanged(value, customerResponseData)
        RetrievingExistingCustomerRequest -> analyticsEventDispatcher.genericStateChanged(value, customerResponseData)
        CreatingCustomerRequest -> { } // Handled separately.
        UpdatingCustomerRequest -> { } // Handled separately.
      }

      // Notify listener of State change.
      callbackListener?.cashAppPayStateDidChange(value)
        .orElse {
          logError(
            "State changed to ${value.javaClass.simpleName}, but no listeners were notified." +
              "Make sure that you've used `registerForStateUpdates` to receive PayKit state updates.",
          )
        }
    }

  init {
    // Register for process lifecycle updates.
    payKitLifecycleListener.register(this)
    analyticsEventDispatcher.sdkInitialized()
  }

  /**
   * Create customer request given a [CashAppPayPaymentAction].
   * Must be called from a background thread.
   *
   * @param paymentAction A wrapper class that contains all of the necessary ingredients for building a customer request.
   *                      Look at [PayKitPaymentAction] for more details.
   */
  @WorkerThread
  override fun createCustomerRequest(paymentAction: CashAppPayPaymentAction) {
    enforceRegisteredStateUpdatesListener()
    currentState = CreatingCustomerRequest

    // Network call.
    val networkResult = networkManager.createCustomerRequest(clientId, paymentAction)
    when (networkResult) {
      is Failure -> {
        currentState = CashAppPayExceptionState(networkResult.exception)
      }

      is Success -> {
        customerResponseData = networkResult.data.customerResponseData
        currentState = ReadyToAuthorize(networkResult.data.customerResponseData)
      }
    }
  }

  /**
   * Update an existing customer request given its [requestId] an the updated definitions contained within [CashAppPayPaymentAction].
   * Must be called from a background thread.
   *
   * @param requestId ID of the request we intent do update.
   * @param paymentAction A wrapper class that contains all of the necessary ingredients for building a customer request.
   *                      Look at [PayKitPaymentAction] for more details.
   */
  @WorkerThread
  override fun updateCustomerRequest(
    requestId: String,
    paymentAction: CashAppPayPaymentAction,
  ) {
    enforceRegisteredStateUpdatesListener()
    currentState = UpdatingCustomerRequest

    // Network request.
    val networkResult = networkManager.updateCustomerRequest(clientId, requestId, paymentAction)
    when (networkResult) {
      is Failure -> {
        currentState = CashAppPayExceptionState(networkResult.exception)
      }

      is Success -> {
        customerResponseData = networkResult.data.customerResponseData
        currentState = ReadyToAuthorize(networkResult.data.customerResponseData)
      }
    }
  }

  @WorkerThread
  override fun startWithExistingCustomerRequest(requestId: String) {
    enforceRegisteredStateUpdatesListener()
    currentState = RetrievingExistingCustomerRequest
    val networkResult = networkManager.retrieveUpdatedRequestData(clientId, requestId)
    when (networkResult) {
      is Failure -> {
        currentState = CashAppPayExceptionState(networkResult.exception)
      }

      is Success -> {
        customerResponseData = networkResult.data.customerResponseData

        // Determine what kind of status we got.
        currentState = when (customerResponseData?.status) {
          STATUS_PENDING -> {
            ReadyToAuthorize(networkResult.data.customerResponseData)
          }

          STATUS_APPROVED -> {
            Approved(networkResult.data.customerResponseData)
          }

          else -> {
            Declined
          }
        }
      }
    }
  }

  /**
   * Authorize a customer request. This function must be called AFTER `createCustomerRequest`.
   * Not doing so will result in an Exception in sandbox mode, and a silent error log in production.
   */
  @Throws(IllegalArgumentException::class, CashAppPayIntegrationException::class)
  override fun authorizeCustomerRequest() {
    val customerData = customerResponseData

    if (customerData == null) {
      logAndSoftCrash(
        CashAppPayIntegrationException(
          "Can't call authorizeCustomerRequest user before calling `createCustomerRequest`. Alternatively provide your own customerData",
        ),
      )
      return
    }

    authorizeCustomerRequest(customerData)
  }

  /**
   * Authorize a customer request with a previously created `customerData`.
   * This function will set this SDK instance internal state to the `customerData` provided here as a function parameter.
   *
   */
  @Throws(IllegalArgumentException::class, RuntimeException::class)
  override fun authorizeCustomerRequest(
    customerData: CustomerResponseData,
  ) {
    enforceRegisteredStateUpdatesListener()

    if (customerData.authFlowTriggers?.mobileUrl.isNullOrEmpty()) {
      throw IllegalArgumentException("customerData is missing redirect url")
    }
    // Open Mobile URL provided by backend response.
    val intent = Intent(Intent.ACTION_VIEW)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    intent.data = try {
      Uri.parse(customerData.authFlowTriggers?.mobileUrl)
    } catch (error: NullPointerException) {
      throw IllegalArgumentException("Cannot parse redirect url")
    }

    // Replace internal state.
    customerResponseData = customerData

    try {
      ApplicationContextHolder.applicationContext.startActivity(intent)
    } catch (activityNotFoundException: ActivityNotFoundException) {
      currentState = CashAppPayExceptionState(CashAppPayIntegrationException("Unable to open mobileUrl: ${customerData.authFlowTriggers?.mobileUrl}"))
      return
    }
    currentState = Authorizing
  }

  /**
   *  Register a [CashAppPayListener] to receive PayKit callbacks.
   */
  override fun registerForStateUpdates(listener: CashAppPayListener) {
    callbackListener = listener
    analyticsEventDispatcher.eventListenerAdded()
  }

  /**
   *  Unregister any previously registered [CashAppPayListener] from PayKit updates.
   */
  override fun unregisterFromStateUpdates() {
    callbackListener = null
    payKitLifecycleListener.unregister(this)
    analyticsEventDispatcher.eventListenerRemoved()
    analyticsEventDispatcher.shutdown()
  }

  private fun enforceRegisteredStateUpdatesListener() {
    if (callbackListener == null) {
      logAndSoftCrash(
        CashAppPayIntegrationException(
          "Shouldn't call this function before registering for state updates via `registerForStateUpdates`.",
        ),
      )
    }
  }

  private fun poolTransactionStatus() {
    Thread {
      val networkResult = networkManager.retrieveUpdatedRequestData(
        clientId,
        customerResponseData!!.id,
      )
      if (networkResult is Failure) {
        currentState = CashAppPayExceptionState(networkResult.exception)
        return@Thread
      }
      customerResponseData = (networkResult as Success).data.customerResponseData

      if (customerResponseData?.status == STATUS_APPROVED) {
        // Successful transaction.
        setStateFinished(true)
      } else {
        // If status is pending, schedule to check again.
        if (customerResponseData?.status == STATUS_PENDING) {
          // TODO: Add backoff strategy for long polling. ( https://www.notion.so/cashappcash/Implement-Long-pooling-retry-logic-a9af47e2db9242faa5d64df2596fd78e )
          Thread.sleep(500)
          poolTransactionStatus()
          return@Thread
        }

        // Unsuccessful transaction.
        setStateFinished(false)
      }
    }.start()
  }

  private fun logError(errorMessage: String) {
    Log.e("PayKit", errorMessage)
  }

  /**
   * This function will log in production, additionally it will throw an exception in sandbox or debug mode.
   */
  @Throws
  private fun logAndSoftCrash(exception: Exception) {
    logError("Error occurred. E.: $exception")
    if (useSandboxEnvironment || BuildConfig.DEBUG) {
      throw exception
    }
  }

  private fun setStateFinished(wasSuccessful: Boolean) {
    currentState = if (wasSuccessful) {
      Approved(customerResponseData!!)
    } else {
      Declined
    }
  }

  /**
   * Lifecycle callbacks.
   */

  override fun onApplicationForegrounded() {
    logError("onApplicationForegrounded")
    if (currentState is Authorizing) {
      currentState = PollingTransactionStatus
      poolTransactionStatus()
    }
  }

  override fun onApplicationBackgrounded() {
    logError("onApplicationBackgrounded")
  }
}
